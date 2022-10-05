package com.shoesbox.domain.post;

import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.shoesbox.domain.friend.Friend;
import com.shoesbox.domain.friend.FriendRepository;
import com.shoesbox.domain.friend.FriendState;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.domain.photo.Photo;
import com.shoesbox.domain.photo.PhotoRepository;
import com.shoesbox.domain.post.dto.PostRequestDto;
import com.shoesbox.domain.post.dto.PostResponseDto;
import com.shoesbox.domain.post.dto.PostResponseListDto;
import com.shoesbox.domain.post.dto.PostUpdateDto;
import com.shoesbox.domain.post.exception.FileCountLimitExceededException;
import com.shoesbox.domain.sse.Alarm;
import com.shoesbox.domain.sse.AlarmRepository;
import com.shoesbox.domain.sse.MessageType;
import com.shoesbox.global.exception.runtime.EntityNotFoundException;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import com.shoesbox.global.util.ImageUtil;
import com.shoesbox.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {
    @Value("${default-images.thumbnail}")
    private String DEFAULT_THUMBNAIL_URL;
    private static final int IMAGE_COUNT_LIMIT = 5;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PhotoRepository photoRepository;
    private final FriendRepository friendRepository;
    private final AlarmRepository alarmRepository;
    private final S3Util s3Util;
    private final ImageUtil imageUtil;
    private final TemporalField fieldISO = WeekFields.of(Locale.KOREA).dayOfWeek();

    // 글 작성
    @Transactional
    public long createPost(long memberId, PostRequestDto postRequestDto) {
        // 이미지 개수 검사
        validateImageCount(postRequestDto.getImageFiles());
        // 날짜 검사
        LocalDate targetDate = validatePostRequest(memberId, postRequestDto);
        // 작성자
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(Member.class.getPackageName()));
        // 게시글 생성
        String thumbnailUrl = "url";
        Post post = Post.builder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .member(member)
                .thumbnailUrl(thumbnailUrl)
                .date(targetDate)
                .build();
        post = postRepository.save(post);

        // 새로운 이미지가 없으면
        if (!validateImageFiles(postRequestDto.getImageFiles())) {
            // 기본값 사용
            thumbnailUrl = DEFAULT_THUMBNAIL_URL;
        } else {
            // 새로운 이미지가 있으면
            // image 업로드 요청 목록 생성
            var imagePutRequests = postRequestDto.getImageFiles().stream()
                    .map(imageUtil::convertToWebp)
                    .map(s3Util::createPutObjectRequest)
                    .collect(Collectors.toList());
            thumbnailUrl = createThumnailFromFile(imagePutRequests.get(0).getFile());
            // 업로드 요청 실행
            var imageUrlsUploaded = s3Util.executePutRequest(imagePutRequests);
            // photo 생성
            var newPhotos = createNewPhotos(imageUrlsUploaded, post);
            // post에 추가
            post.getPhotos().addAll(newPhotos);
        }
        // post에 썸네일 삽입
        post.setThumbnailUrl(thumbnailUrl);

        // 모든 친구에 알림 생성 및 발송
        notifyAddPostEvent(memberId, post);

        return post.getId();
    }

    // 전체 조회
    @Transactional(readOnly = true)
    public List<PostResponseListDto> getPosts(long currentMemberId, long targetId, int year, int month) {
        checkAuthorization(currentMemberId, targetId);

        // 찾으려는 달의 첫 번째 일요일의 날짜를 구한다
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate firstMonday = firstDay.with(fieldISO, 1);

        // 찾으려는 달의 마지막 토요일의 날짜를 구한다
        LocalDate lastDay = LocalDate.of(year, month, firstDay.getMonth().maxLength());
        LocalDate lastSaturday = lastDay.with(fieldISO, 7);

        // 총 몇 주를 표시해야 하는지 계산한다.
        int daysTotal = (int) (ChronoUnit.WEEKS.between(firstMonday, lastSaturday) + 1) * 7;

        // 작성자의 memberId가 일치하고, firstDay와 lastDay 사이에 작성된 글을 검색한다.
        var foundPosts = getPostsByDate(targetId, firstMonday, lastSaturday);

        // 달력 일자의 개수와 같은 크기의 배열 생성
        var posts = new PostResponseListDto[daysTotal];
        var index = 0;
        for (int i = 0; i < posts.length; i++) {
            // 오늘이 몇 일인지 계산
            // 달력 첫날부터 하루하루 증가
            var today = firstMonday.plusDays(i);
            if (index < foundPosts.length) {
                // 게시글의 작성일이 오늘과 일치할 경우 반환할 posts 배열에 대입
                if (foundPosts[index].getDate().isEqual(today)) {
                    posts[i] = foundPosts[index];
                    ++index;
                    continue;
                }
            }
            // 작성일이 일치하는 날이 없다면 일기를 쓰지 않은 날이다.
            // 빈 객체를 생성해서 넣어준다.
            posts[i] = PostResponseListDto.builder()
                    .postId(0)
                    .thumbnailUrl(null)
                    .createdYear(today.getYear())
                    .createdMonth(today.getMonthValue())
                    .createdDay(today.getDayOfMonth())
                    .build();
        }
        return Arrays.asList(posts);
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public PostResponseDto getPost(long currentMemberId, long postId) {
        Post post = getPost(postId);
        checkAuthorization(currentMemberId, post.getMemberId());
        return toPostResponseDto(post);
    }

    // 수정
    @Transactional
    public long updatePost(long currentMemberId, long postId, PostUpdateDto postUpdateDto) {
        Post post = getPost(postId);
        // 수정 권한이 있는지 검사
        checkSelfAuthorization(currentMemberId, post.getMemberId());
        // 이미지 개수 검사
        List<Long> photoIdsToDelete = postUpdateDto.getImagesToDelete();
        List<MultipartFile> imagesToUpload = postUpdateDto.getImageFiles();
        List<Photo> photos = post.getPhotos();
        validateImageCount(photos, photoIdsToDelete, imagesToUpload);

        // 기존 이미지 중 삭제할 게 있는지
        DeleteObjectsRequest deleteRequests = null;
        boolean hasImagesToDelete = validateImagesToDelete(photoIdsToDelete);
        if (hasImagesToDelete) {
            // 해당 이미지를 찾아서 삭제 요청 생성
            var imageUrlsToDelete = deletePhotos(photos, photoIdsToDelete);
            deleteRequests = s3Util.createDeleteRequest(imageUrlsToDelete);
        }
        // 새로 업로드할 이미지가 있는지
        List<PutObjectRequest> putRequests = new ArrayList<>();
        boolean hasImagesToUpload = validateImageFiles(imagesToUpload);
        if (hasImagesToUpload) {
            // image 업로드 요청 생성
            putRequests.addAll(postUpdateDto.getImageFiles().stream()
                                       .map(imageUtil::convertToWebp)
                                       .map(s3Util::createPutObjectRequest)
                                       .collect(Collectors.toList()));
        }

        // 삭제할 것도, 업로드할 것도 없다면 제목, 본문만 수정
        if (!hasImagesToDelete && !hasImagesToUpload) {
            // 수정내역 db 반영
            post.update(postUpdateDto.getTitle(), postUpdateDto.getContent(), null);
            return post.getId();
        }

        if (hasImagesToDelete) {
            // 삭제 요청 실행
            s3Util.executeDeleteRequest(deleteRequests);
        }
        if (hasImagesToUpload) {
            // 업로드 요청 실행
            var imageUrlsUploaded = s3Util.executePutRequest(putRequests);
            // photo 생성
            var newPhotos = createNewPhotos(imageUrlsUploaded, post);
            // post에 추가
            photos.addAll(newPhotos);
        }
        // 썸네일 생성
        String thumbnailUrl;
        if (photos.size() == 0) {
            thumbnailUrl = DEFAULT_THUMBNAIL_URL;
        } else {
            thumbnailUrl = createThumnailFromUrl(photos.get(0).getUrl());
        }
        // 기존 썸네일 삭제
        var deleteRequest = s3Util.createDeleteRequest(post.getThumbnailUrl());
        s3Util.executeDeleteRequest(deleteRequest);
        // 수정내역 db 반영
        post.update(postUpdateDto.getTitle(), postUpdateDto.getContent(), thumbnailUrl);
        return post.getId();
    }

    // 삭제
    @Transactional
    public long deletePost(long currentMemberId, long postId) {
        Post post = getPost(postId);
        checkSelfAuthorization(currentMemberId, post.getMemberId());
        // 첨부 이미지 삭제
        deleteAllPhotosInPost(post);
        postRepository.deleteById(postId);
        return postId;
    }

    // 강제 삭제
    @Transactional
    public long deletePostAdmin(long postId) {
        Post post = getPost(postId);
        // 첨부 이미지 삭제
        deleteAllPhotosInPost(post);
        postRepository.deleteById(postId);
        return postId;
    }

    @Transactional
    public long deleteAllPosts(long currentMemberId) {
        Member member = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new EntityNotFoundException(Member.class.getPackageName()));
        member.getPosts().forEach(this::deleteAllPhotosInPost);
        member.getPosts().clear();

        if (member.getPosts().size() > 0) {
            log.debug("글 삭제 실패!");
        } else {
            log.debug("모든 글을 삭제했습니다!");
        }
        return member.getId();
    }

    private Post getPost(long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class.getPackageName()));
    }

    private PostResponseListDto[] getPostsByDate(long memberId, LocalDate firstDay, LocalDate lastDay) {
        return postRepository.findAllByMemberIdAndDateBetween(memberId, firstDay, lastDay)
                .stream()
                // PostListResponseDto의 배열로 변환한다.
                .map(this::toPostListResponseDto)
                .sorted(Comparator.comparing(PostResponseListDto::getCreatedMonth)
                                .thenComparing(PostResponseListDto::getCreatedDay))
                .toArray(PostResponseListDto[]::new);
    }

    private PostResponseDto toPostResponseDto(Post post) {
        var images = new LinkedHashMap<Long, String>();
        for (var photo : post.getPhotos()) {
            images.put(photo.getId(), photo.getUrl());
        }
        return PostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .nickname(post.getMember().getNickname())
                .memberId(post.getMemberId())
                .images(images)
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .build();
    }

    private PostResponseListDto toPostListResponseDto(Post post) {
        return PostResponseListDto.builder()
                .postId(post.getId())
                .thumbnailUrl(post.getThumbnailUrl())
                .createdYear(post.getDate().getYear())
                .createdMonth(post.getDate().getMonthValue())
                .createdDay(post.getDate().getDayOfMonth())
                .date(post.getDate())
                .build();
    }

    private List<Photo> createNewPhotos(List<String> imageUrls, Post post) {
        List<Photo> photos = new ArrayList<>();
        for (var url : imageUrls) {
            Photo photo = Photo.builder()
                    .url(url)
                    .post(post)
                    .member(post.getMember())
                    .build();
            photoRepository.save(photo);
            photos.add(photo);
        }
        return photos;
    }

    private List<String> deletePhotos(List<Photo> photos, List<Long> photoIdsToDelete) {
        List<String> imageUrlsToDelete = new ArrayList<>();
        Predicate<Photo> predictable = photo -> photoIdsToDelete.contains(photo.getId());
        // 삭제할 url
        photos.stream()
                .filter(predictable)
                .forEach((photo) -> imageUrlsToDelete.add(photo.getUrl()));
        // db에서 삭제
        photos.removeIf(predictable);
        if (imageUrlsToDelete.size() != photoIdsToDelete.size()) {
            throw new IllegalArgumentException("존재하지 않는 photoId입니다.");
        }
        return imageUrlsToDelete;
    }

    private void deleteAllPhotosInPost(Post post) {
        if (post.getPhotos() != null) {
            // s3 버킷에서 기존 이미지 삭제
            List<String> urls = post.getPhotos().stream()
                    .map((Photo::getUrl))
                    .collect(Collectors.toList());
            s3Util.executeDeleteRequest(s3Util.createDeleteRequest(urls));
            post.getPhotos().clear();
        }
    }

    private String createThumnailFromFile(File file) {
        // 썸네일용 파일 생성 및 업로드 요청 생성
        File thumbnailFile = imageUtil.resizeImage(file);
        var thumbnailPutRequest = s3Util.createPutObjectRequest(thumbnailFile);
        // 이미지 업로드
        var thumbnailUrl = s3Util.executePutRequest(thumbnailPutRequest);
        // 임시파일 삭제
        thumbnailFile.delete();
        return thumbnailUrl;
    }

    private String createThumnailFromUrl(String url) {
        File originalFile;
        File thumbnailFile;
        // url로부터 이미지 파일 다운로드
        originalFile = s3Util.getFileFromUrl(url);
        // 다운 받은 이미지로부터 썸네일 생성
        thumbnailFile = imageUtil.resizeImage(originalFile);
        originalFile.delete();
        // 썸네일 업로드 요청 생성
        var thumbnailPutRequest = s3Util.createPutObjectRequest(thumbnailFile);
        // 썸네일 업로드
        return s3Util.executePutRequest(thumbnailPutRequest);
    }

    // 첨부 이미지 개수 체크
    private boolean validateImageCount(List<MultipartFile> imageFiles) {
        if (imageFiles != null && imageFiles.size() > IMAGE_COUNT_LIMIT) {
            throw new FileCountLimitExceededException("이미지가 " + IMAGE_COUNT_LIMIT + "개를 초과하였습니다.");
        }
        return true;
    }

    private boolean validateImageCount(List<Photo> photos, List<Long> imagesToDelete, List<MultipartFile> imagesToUpload) {
        if (photos != null) {
            int imagesLeftCount = photos.size();
            int imagesToDeleteCount = (imagesToDelete == null) ? 0 : imagesToDelete.size();
            int imagesUploadedCount = (imagesToUpload == null) ? 0 : imagesToUpload.size();
            if (imagesLeftCount - imagesToDeleteCount + imagesUploadedCount > IMAGE_COUNT_LIMIT) {
                throw new FileCountLimitExceededException("이미지가 " + IMAGE_COUNT_LIMIT + "개를 초과하였습니다.");
            }
        } else {
            throw new IllegalArgumentException("post.getPhotos()가 null입니다.");
        }
        return true;
    }

    // 이미지가 있으면 true
    private boolean validateImageFiles(List<MultipartFile> imageFiles) {
        return (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.get(0).isEmpty());
    }

    // 삭제할 이미지가 있으면 true
    private boolean validateImagesToDelete(List<Long> imagesToDelete) {
        return (imagesToDelete != null && !imagesToDelete.isEmpty());
    }

    private LocalDate validatePostRequest(long memberId, PostRequestDto postRequestDto) {
        LocalDate targetDate = LocalDate.of(postRequestDto.getYear(), postRequestDto.getMonth(),
                                            postRequestDto.getDay());
        // 연도 검사
        if (targetDate.getYear() != LocalDate.now().getYear()) {
            throw new IllegalArgumentException("연도를 잘못 입력하였습니다.");
        }
        // 작성 기한 검사
        LocalDate aMonthBefore = LocalDate.of(
                LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1, LocalDate.now().getDayOfMonth());
        if (targetDate.isBefore(aMonthBefore)) {
            throw new IllegalArgumentException("오늘로부터 최대 1달 전의 날까지 일기를 작성할 수 있습니다.");
        } else if (targetDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("미래의 일기를 미리 작성할 수 없습니다.");
        }
        // 중복 검사
        checkDuplicatePost(memberId, targetDate);

        return targetDate;
    }

    private void checkDuplicatePost(long memberId, LocalDate targetDate) {
        if (postRepository.existsByMemberIdAndDate(memberId, targetDate)) {
            throw new IllegalArgumentException("일기를 이미 작성하였습니다.");
        }
    }

    private void checkSelfAuthorization(long currentMemberId, long targetId) {
        if (currentMemberId != targetId) {
            throw new UnAuthorizedException("접근 권한이 없습니다.");
        }
    }

    private void checkAuthorization(long currentMemberId, long targetId) {
        if (currentMemberId != targetId && !isFriend(currentMemberId, targetId)) {
            throw new UnAuthorizedException("접근 권한이 없습니다.");
        }
    }

    private boolean isFriend(long currentMemberId, long targetId) {
        return friendRepository.existsByFromMemberIdAndToMemberIdAndFriendState(
                targetId, currentMemberId, FriendState.FRIEND) ||
                friendRepository.existsByFromMemberIdAndToMemberIdAndFriendState(
                        currentMemberId, targetId, FriendState.FRIEND);
    }

    public void notifyAddPostEvent(long senderMemberId, Post post) {
        // 친구 요청을 한 리스트
        List<Friend> friends = friendRepository.findAllByToMemberIdAndFriendState(senderMemberId, FriendState.FRIEND);

        // 친구 요청을 받은 리스트
        friends.addAll(friendRepository.findAllByFromMemberIdAndFriendState(senderMemberId, FriendState.FRIEND));

        // 알람에 저장할 날짜 객체 생성
        int month = post.getDate().getMonthValue();
        int day = post.getDate().getDayOfMonth();
        long postId = post.getId();

        for (Friend friend : friends) {
//            long receiverMemberId = friend.getToMember().getId();
//            if (sseEmitters.containsKey(receiverMemberId) && receiverMemberId != senderMemberId) {
//                MessageDto messgeDto = MessageDto.builder()
//                        .msgType("Post")
//                        .senderNickName(senderNickName)
//                        .postId(postId)
//                        .month(month)
//                        .day(day)
//                        .build();
//                SseEmitter sseEmitter = sseEmitters.get(receiverMemberId);
//
////                sseExecutor.execute(() -> {
////                    try {
////                        sseEmitter.send(
////                                SseEmitter.event()
////                                        .name("addPost")
////                                        .data(messgeDto, MediaType.APPLICATION_JSON));
////                        log.info(">>>>>>>>>>>>>> Sent the Alarm Message from : " + senderNickName + " by POST EVENT.");
////                        Thread.sleep(100);
////                    } catch (IOException | InterruptedException e) {
////                        log.error(e.getLocalizedMessage());
////                        sseEmitter.completeWithError(e);
////                    }
////                });
//                try {
//                    sseEmitter.send(
//                            SseEmitter.event()
//                                    .name("addPost")
//                                    .data(messgeDto, MediaType.APPLICATION_JSON));
//                    log.info(">>>>>>>>>>>>>> Sent the Alarm Message from : " + senderNickName + " by POST EVENT.");
//                    Thread.sleep(100);
//                } catch (IOException | InterruptedException e) {
//                    log.error(e.getLocalizedMessage());
//                    sseEmitter.completeWithError(e);
//                }
//            }

            long friendMemberId;
            if (friend.getToMember().getId() == senderMemberId) {
                friendMemberId = friend.getFromMember().getId();
            } else {
                friendMemberId = friend.getToMember().getId();
            }
            // 알림 내용 db에 저장
            saveAlarm(senderMemberId, friendMemberId, postId, month, day);
        }
    }

    @Transactional
    public void saveAlarm(long senderMemberId, long receiverMemberId, long contentId, int month, int day) {
        String content = contentId + "," + month + "," + day;

        // send: 댓글작성자, receive: 글작성자
        Member senderMember = Member.builder()
                .id(senderMemberId)
                .build();

        Alarm alarm = Alarm.builder()
                .senderMember(senderMember)
                .receiverMemberId(receiverMemberId)
                .content(content)
                .messageType(MessageType.POST)
                .build();

        alarmRepository.save(alarm);
    }
}
