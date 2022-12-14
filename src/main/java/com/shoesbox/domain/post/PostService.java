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

    // ??? ??????
    @Transactional
    public long createPost(long memberId, PostRequestDto postRequestDto) {
        // ????????? ?????? ??????
        validateImageCount(postRequestDto.getImageFiles());
        // ?????? ??????
        LocalDate targetDate = validatePostRequest(memberId, postRequestDto);
        // ?????????
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(Member.class.getPackageName()));
        // ????????? ??????
        String thumbnailUrl = "url";
        Post post = Post.builder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .member(member)
                .thumbnailUrl(thumbnailUrl)
                .date(targetDate)
                .build();
        post = postRepository.save(post);

        // ????????? ???????????? ?????????
        if (!validateImageFiles(postRequestDto.getImageFiles())) {
            // ????????? ??????
            thumbnailUrl = DEFAULT_THUMBNAIL_URL;
        } else {
            // ????????? ???????????? ?????????
            // image ????????? ?????? ?????? ??????
            var imagePutRequests = postRequestDto.getImageFiles().stream()
                    .map(imageUtil::convertToWebp)
                    .map(s3Util::createPutObjectRequest)
                    .collect(Collectors.toList());
            thumbnailUrl = createThumnailFromFile(imagePutRequests.get(0).getFile());
            // ????????? ?????? ??????
            var imageUrlsUploaded = s3Util.executePutRequest(imagePutRequests);
            // photo ??????
            var newPhotos = createNewPhotos(imageUrlsUploaded, post);
            // post??? ??????
            post.getPhotos().addAll(newPhotos);
        }
        // post??? ????????? ??????
        post.setThumbnailUrl(thumbnailUrl);

        // ?????? ????????? ?????? ?????? ??? ??????
        notifyAddPostEvent(memberId, post);

        return post.getId();
    }

    // ?????? ??????
    @Transactional(readOnly = true)
    public List<PostResponseListDto> getPosts(long currentMemberId, long targetId, int year, int month) {
        checkAuthorization(currentMemberId, targetId);

        // ???????????? ?????? ??? ?????? ???????????? ????????? ?????????
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate firstMonday = firstDay.with(fieldISO, 1);

        // ???????????? ?????? ????????? ???????????? ????????? ?????????
        LocalDate lastDay = LocalDate.of(year, month, firstDay.getMonth().maxLength());
        LocalDate lastSaturday = lastDay.with(fieldISO, 7);

        // ??? ??? ?????? ???????????? ????????? ????????????.
        int daysTotal = (int) (ChronoUnit.WEEKS.between(firstMonday, lastSaturday) + 1) * 7;

        // ???????????? memberId??? ????????????, firstDay??? lastDay ????????? ????????? ?????? ????????????.
        var foundPosts = getPostsByDate(targetId, firstMonday, lastSaturday);

        // ?????? ????????? ????????? ?????? ????????? ?????? ??????
        var posts = new PostResponseListDto[daysTotal];
        var index = 0;
        for (int i = 0; i < posts.length; i++) {
            // ????????? ??? ????????? ??????
            // ?????? ???????????? ???????????? ??????
            var today = firstMonday.plusDays(i);
            if (index < foundPosts.length) {
                // ???????????? ???????????? ????????? ????????? ?????? ????????? posts ????????? ??????
                if (foundPosts[index].getDate().isEqual(today)) {
                    posts[i] = foundPosts[index];
                    ++index;
                    continue;
                }
            }
            // ???????????? ???????????? ?????? ????????? ????????? ?????? ?????? ?????????.
            // ??? ????????? ???????????? ????????????.
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

    // ?????? ??????
    @Transactional(readOnly = true)
    public PostResponseDto getPost(long currentMemberId, long postId) {
        Post post = getPost(postId);
        checkAuthorization(currentMemberId, post.getMemberId());
        return toPostResponseDto(post);
    }

    // ??????
    @Transactional
    public long updatePost(long currentMemberId, long postId, PostUpdateDto postUpdateDto) {
        Post post = getPost(postId);
        // ?????? ????????? ????????? ??????
        checkSelfAuthorization(currentMemberId, post.getMemberId());
        // ????????? ?????? ??????
        List<Long> photoIdsToDelete = postUpdateDto.getImagesToDelete();
        List<MultipartFile> imagesToUpload = postUpdateDto.getImageFiles();
        List<Photo> photos = post.getPhotos();
        validateImageCount(photos, photoIdsToDelete, imagesToUpload);

        // ?????? ????????? ??? ????????? ??? ?????????
        DeleteObjectsRequest deleteRequests = null;
        boolean hasImagesToDelete = validateImagesToDelete(photoIdsToDelete);
        if (hasImagesToDelete) {
            // ?????? ???????????? ????????? ?????? ?????? ??????
            var imageUrlsToDelete = deletePhotos(photos, photoIdsToDelete);
            deleteRequests = s3Util.createDeleteRequest(imageUrlsToDelete);
        }
        // ?????? ???????????? ???????????? ?????????
        List<PutObjectRequest> putRequests = new ArrayList<>();
        boolean hasImagesToUpload = validateImageFiles(imagesToUpload);
        if (hasImagesToUpload) {
            // image ????????? ?????? ??????
            putRequests.addAll(postUpdateDto.getImageFiles().stream()
                                       .map(imageUtil::convertToWebp)
                                       .map(s3Util::createPutObjectRequest)
                                       .collect(Collectors.toList()));
        }

        // ????????? ??????, ???????????? ?????? ????????? ??????, ????????? ??????
        if (!hasImagesToDelete && !hasImagesToUpload) {
            // ???????????? db ??????
            post.update(postUpdateDto.getTitle(), postUpdateDto.getContent(), null);
            return post.getId();
        }

        if (hasImagesToDelete) {
            // ?????? ?????? ??????
            s3Util.executeDeleteRequest(deleteRequests);
        }
        if (hasImagesToUpload) {
            // ????????? ?????? ??????
            var imageUrlsUploaded = s3Util.executePutRequest(putRequests);
            // photo ??????
            var newPhotos = createNewPhotos(imageUrlsUploaded, post);
            // post??? ??????
            photos.addAll(newPhotos);
        }
        // ????????? ??????
        String thumbnailUrl;
        if (photos.size() == 0) {
            thumbnailUrl = DEFAULT_THUMBNAIL_URL;
        } else {
            thumbnailUrl = createThumnailFromUrl(photos.get(0).getUrl());
        }
        // ?????? ????????? ??????
        var deleteRequest = s3Util.createDeleteRequest(post.getThumbnailUrl());
        s3Util.executeDeleteRequest(deleteRequest);
        // ???????????? db ??????
        post.update(postUpdateDto.getTitle(), postUpdateDto.getContent(), thumbnailUrl);
        return post.getId();
    }

    // ??????
    @Transactional
    public long deletePost(long currentMemberId, long postId) {
        Post post = getPost(postId);
        checkSelfAuthorization(currentMemberId, post.getMemberId());
        // ?????? ????????? ??????
        deleteAllPhotosInPost(post);
        postRepository.deleteById(postId);
        return postId;
    }

    // ?????? ??????
    @Transactional
    public long deletePostAdmin(long postId) {
        Post post = getPost(postId);
        // ?????? ????????? ??????
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
            log.debug("??? ?????? ??????!");
        } else {
            log.debug("?????? ?????? ??????????????????!");
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
                // PostListResponseDto??? ????????? ????????????.
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
        // ????????? url
        photos.stream()
                .filter(predictable)
                .forEach((photo) -> imageUrlsToDelete.add(photo.getUrl()));
        // db?????? ??????
        photos.removeIf(predictable);
        if (imageUrlsToDelete.size() != photoIdsToDelete.size()) {
            throw new IllegalArgumentException("???????????? ?????? photoId?????????.");
        }
        return imageUrlsToDelete;
    }

    private void deleteAllPhotosInPost(Post post) {
        if (post.getPhotos() != null) {
            // s3 ???????????? ?????? ????????? ??????
            List<String> urls = post.getPhotos().stream()
                    .map((Photo::getUrl))
                    .collect(Collectors.toList());
            s3Util.executeDeleteRequest(s3Util.createDeleteRequest(urls));
            post.getPhotos().clear();
        }
    }

    private String createThumnailFromFile(File file) {
        // ???????????? ?????? ?????? ??? ????????? ?????? ??????
        File thumbnailFile = imageUtil.resizeImage(file);
        var thumbnailPutRequest = s3Util.createPutObjectRequest(thumbnailFile);
        // ????????? ?????????
        var thumbnailUrl = s3Util.executePutRequest(thumbnailPutRequest);
        // ???????????? ??????
        thumbnailFile.delete();
        return thumbnailUrl;
    }

    private String createThumnailFromUrl(String url) {
        File originalFile;
        File thumbnailFile;
        // url????????? ????????? ?????? ????????????
        originalFile = s3Util.getFileFromUrl(url);
        // ?????? ?????? ?????????????????? ????????? ??????
        thumbnailFile = imageUtil.resizeImage(originalFile);
        originalFile.delete();
        // ????????? ????????? ?????? ??????
        var thumbnailPutRequest = s3Util.createPutObjectRequest(thumbnailFile);
        // ????????? ?????????
        return s3Util.executePutRequest(thumbnailPutRequest);
    }

    // ?????? ????????? ?????? ??????
    private boolean validateImageCount(List<MultipartFile> imageFiles) {
        if (imageFiles != null && imageFiles.size() > IMAGE_COUNT_LIMIT) {
            throw new FileCountLimitExceededException("???????????? " + IMAGE_COUNT_LIMIT + "?????? ?????????????????????.");
        }
        return true;
    }

    private boolean validateImageCount(List<Photo> photos, List<Long> imagesToDelete, List<MultipartFile> imagesToUpload) {
        if (photos != null) {
            int imagesLeftCount = photos.size();
            int imagesToDeleteCount = (imagesToDelete == null) ? 0 : imagesToDelete.size();
            int imagesUploadedCount = (imagesToUpload == null) ? 0 : imagesToUpload.size();
            if (imagesLeftCount - imagesToDeleteCount + imagesUploadedCount > IMAGE_COUNT_LIMIT) {
                throw new FileCountLimitExceededException("???????????? " + IMAGE_COUNT_LIMIT + "?????? ?????????????????????.");
            }
        } else {
            throw new IllegalArgumentException("post.getPhotos()??? null?????????.");
        }
        return true;
    }

    // ???????????? ????????? true
    private boolean validateImageFiles(List<MultipartFile> imageFiles) {
        return (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.get(0).isEmpty());
    }

    // ????????? ???????????? ????????? true
    private boolean validateImagesToDelete(List<Long> imagesToDelete) {
        return (imagesToDelete != null && !imagesToDelete.isEmpty());
    }

    private LocalDate validatePostRequest(long memberId, PostRequestDto postRequestDto) {
        LocalDate targetDate = LocalDate.of(postRequestDto.getYear(), postRequestDto.getMonth(),
                                            postRequestDto.getDay());
        // ?????? ??????
        if (targetDate.getYear() != LocalDate.now().getYear()) {
            throw new IllegalArgumentException("????????? ?????? ?????????????????????.");
        }
        // ?????? ?????? ??????
        LocalDate aMonthBefore = LocalDate.of(
                LocalDate.now().getYear(), LocalDate.now().getMonthValue() - 1, LocalDate.now().getDayOfMonth());
        if (targetDate.isBefore(aMonthBefore)) {
            throw new IllegalArgumentException("??????????????? ?????? 1??? ?????? ????????? ????????? ????????? ??? ????????????.");
        } else if (targetDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("????????? ????????? ?????? ????????? ??? ????????????.");
        }
        // ?????? ??????
        checkDuplicatePost(memberId, targetDate);

        return targetDate;
    }

    private void checkDuplicatePost(long memberId, LocalDate targetDate) {
        if (postRepository.existsByMemberIdAndDate(memberId, targetDate)) {
            throw new IllegalArgumentException("????????? ?????? ?????????????????????.");
        }
    }

    private void checkSelfAuthorization(long currentMemberId, long targetId) {
        if (currentMemberId != targetId) {
            throw new UnAuthorizedException("?????? ????????? ????????????.");
        }
    }

    private void checkAuthorization(long currentMemberId, long targetId) {
        if (currentMemberId != targetId && !isFriend(currentMemberId, targetId)) {
            throw new UnAuthorizedException("?????? ????????? ????????????.");
        }
    }

    private boolean isFriend(long currentMemberId, long targetId) {
        return friendRepository.existsByFromMemberIdAndToMemberIdAndFriendState(
                targetId, currentMemberId, FriendState.FRIEND) ||
                friendRepository.existsByFromMemberIdAndToMemberIdAndFriendState(
                        currentMemberId, targetId, FriendState.FRIEND);
    }

    public void notifyAddPostEvent(long senderMemberId, Post post) {
        // ?????? ????????? ??? ?????????
        List<Friend> friends = friendRepository.findAllByToMemberIdAndFriendState(senderMemberId, FriendState.FRIEND);

        // ?????? ????????? ?????? ?????????
        friends.addAll(friendRepository.findAllByFromMemberIdAndFriendState(senderMemberId, FriendState.FRIEND));

        // ????????? ????????? ?????? ?????? ??????
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
            // ?????? ?????? db??? ??????
            saveAlarm(senderMemberId, friendMemberId, postId, month, day);
        }
    }

    @Transactional
    public void saveAlarm(long senderMemberId, long receiverMemberId, long contentId, int month, int day) {
        String content = contentId + "," + month + "," + day;

        // send: ???????????????, receive: ????????????
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
