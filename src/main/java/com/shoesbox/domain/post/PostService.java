package com.shoesbox.domain.post;

import com.shoesbox.domain.friend.Friend;
import com.shoesbox.domain.friend.FriendRepository;
import com.shoesbox.domain.friend.FriendState;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.domain.photo.Photo;
import com.shoesbox.domain.photo.PhotoRepository;
import com.shoesbox.domain.photo.PhotoService;
import com.shoesbox.domain.photo.S3Service;
import com.shoesbox.domain.photo.exception.ImageUploadFailureException;
import com.shoesbox.domain.post.dto.PostRequestDto;
import com.shoesbox.domain.post.dto.PostResponseDto;
import com.shoesbox.domain.post.dto.PostResponseListDto;
import com.shoesbox.domain.post.dto.PostUpdateDto;
import com.shoesbox.domain.sse.Alarm;
import com.shoesbox.domain.sse.AlarmRepository;
import com.shoesbox.domain.sse.MessageType;
import com.shoesbox.global.exception.runtime.EntityNotFoundException;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class PostService {
    @Value("${default-images.thumbnail}")
    private String DEFAULT_THUMBNAIL_URL;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PhotoRepository photoRepository;
    private final FriendRepository friendRepository;
    private final AlarmRepository alarmRepository;
    private final PhotoService photoService;
    private final S3Service s3Service;
    private final TemporalField fieldISO = WeekFields.of(Locale.KOREA).dayOfWeek();

    // 글 작성
    @Transactional
    public long createPost(long memberId, PostRequestDto postRequestDto) {
        // 날짜 검사
        LocalDate targetDate = validatePostRequest(memberId, postRequestDto);
        String thumbnailUrl;
        List<File> files = new ArrayList<>();
        // 새로운 이미지가 없으면
        if (!validateImageFiles(postRequestDto.getImageFiles())) {
            // 기본값 사용
            thumbnailUrl = DEFAULT_THUMBNAIL_URL;
        } else {
            // Multipart File -> File, file 회전 여부 수정
            files = checkImageRotation(postRequestDto.getImageFiles());
            // 새로운 이미지가 있으면 썸네일 업로드
            thumbnailUrl = s3Service.uploadThumbnail(files.get(0));
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(Member.class.getPackageName()));
        // 게시글 생성
        Post post = Post.builder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .member(member)
                .thumbnailUrl(thumbnailUrl)
                .date(targetDate)
                .build();
        post = postRepository.save(post);

        // 썸네일이 기본값이 아니면
        // photo 생성
        if (!thumbnailUrl.equals(DEFAULT_THUMBNAIL_URL)) {
            createPhoto(files, post);
            // 로컬 파일 삭제
            for(File file : files){
                file.delete();
            }
        }

        // 모든 친구에 알림 생성 및 발송
        notifyAddPostEvent(memberId, post, member.getNickname());

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
    public PostResponseDto updatePost(long currentMemberId, long postId, PostUpdateDto postUpdateDto) {
        Post post = getPost(postId);

        // 수정 권한이 있는지 검사
        checkSelfAuthorization(currentMemberId, post.getMemberId());

        // 새로운 이미지가 없으면
        if (!validateImageFiles(postUpdateDto.getImageFiles())) {
            // 기존 썸네일 재활용
            post.update(postUpdateDto.getTitle(), postUpdateDto.getContent(), post.getThumbnailUrl());
            return toPostResponseDto(post);
        } else {
            // 새로운 이미지가 있는데
            // 기존 이미지가 있으면
            if (!post.getPhotos().isEmpty()) {
                // 기존 썸네일, 사진 삭제
                s3Service.deleteObjectByImageUrl(post.getThumbnailUrl());
                deletePhotosInPost(post);
            }
        }

        // Multipart File -> File, file 회전 여부 수정
        List<File> files = checkImageRotation(postUpdateDto.getImageFiles());
        // 썸네일 생성
        String thumbnailUrl = s3Service.uploadThumbnail(files.get(0));
        // 첨부 이미지 저장
        createPhoto(files, post);
        post.update(postUpdateDto.getTitle(), postUpdateDto.getContent(), thumbnailUrl);
        // 로컬 파일 삭제
        for(File file : files){
            file.delete();
        }
        return toPostResponseDto(post);
    }

    // 삭제
    @Transactional
    public long deletePost(long currentMemberId, long postId) {
        Post post = getPost(postId);
        checkSelfAuthorization(currentMemberId, post.getMemberId());

        // 첨부 이미지가 있으면 삭제
        if (!post.getPhotos().isEmpty()) {
            deletePhotosInPost(post);
            s3Service.deleteObjectByImageUrl(post.getThumbnailUrl());
        }
        postRepository.deleteById(postId);
        return postId;
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
        var urls = new ArrayList<String>();
        for (var photo : post.getPhotos()) {
            urls.add(photo.getUrl());
        }
        return PostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .nickname(post.getMember().getNickname())
                .memberId(post.getMemberId())
                .images(urls)
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

    private void createPhoto(List<File> imageFiles, Post post) {
        var photos = new ArrayList<Photo>();
        for (var imageFile : imageFiles) {
            var uploadedImageUrl = s3Service.uploadImage(imageFile);
            Photo photo = Photo.builder()
                    .url(uploadedImageUrl)
                    .post(post)
                    .member(post.getMember())
                    .build();
            photoRepository.save(photo);
            photos.add(photo);
        }
        if (post.getPhotos() != null) {
            post.getPhotos().clear();
            post.getPhotos().addAll(photos);
        }
    }

    private void deletePhotosInPost(Post post) {
        if (post.getPhotos() != null) {
            // s3 버킷에서 기존 이미지 삭제
            for (var photo : post.getPhotos()) {
                s3Service.deleteObjectByImageUrl(photo.getUrl());
            }
            post.getPhotos().clear();
        }
    }

    // 이미지 회전 여부 검사
    private List<File> checkImageRotation(List<MultipartFile> imageFiles) {
        List<File> files = new ArrayList<>();
        for (MultipartFile mFile : imageFiles) {
            String fileName = Objects.requireNonNull(mFile.getOriginalFilename()).toLowerCase();
            // todo : 확장자 검사 중복됨, 정리필
            if (!(fileName.endsWith(".bmp")
                    || fileName.endsWith(".jpg")
                    || fileName.endsWith(".jpeg")
                    || fileName.endsWith(".png"))) {
                throw new ImageUploadFailureException("이미지 파일 형식은 bmp, jpg, jpeg, png 중 하나여야 합니다.");
            }
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

            // 회전 방향 : 1인 경우 정상
            int orientation = photoService.getOrientation(mFile);
            try {
                BufferedImage rotatedImage = photoService.rotateImageForMobile(mFile.getInputStream(), orientation);
                File file = new File(mFile.getOriginalFilename());
                ImageIO.write(rotatedImage, fileExtension, file);
                files.add(file);
            } catch (IOException e) {
                files.add((File) mFile);
                throw new RuntimeException("POST SERVICE - IMAGE_ROTATION : " + e);
            }


        }
        return files;
    }

    // 이미지가 있으면 true
    private boolean validateImageFiles(List<MultipartFile> imageFiles) {
        return (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.get(0).isEmpty());
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

    public void notifyAddPostEvent(long senderMemberId, Post post, String senderNickName) {
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
