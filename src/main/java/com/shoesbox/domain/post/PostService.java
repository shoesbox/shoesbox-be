package com.shoesbox.domain.post;

import com.shoesbox.domain.comment.CommentResponseDto;
import com.shoesbox.domain.comment.CommentService;
import com.shoesbox.domain.friend.FriendService;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.photo.Photo;
import com.shoesbox.domain.photo.PhotoRepository;
import com.shoesbox.domain.photo.S3Service;
import com.shoesbox.domain.post.dto.PostListResponseDto;
import com.shoesbox.domain.post.dto.PostRequestDto;
import com.shoesbox.domain.post.dto.PostResponseDto;
import com.shoesbox.global.exception.runtime.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {

    private final String defaultThumbnailImageAddress = "https://shoesbox-sparta.s3.ap-northeast-2.amazonaws.com/_________.jpg";
    private final PostRepository postRepository;
    private final PhotoRepository photoRepository;
    private final FriendService friendService;
    private final S3Service s3Service;

    // 생성
    @Transactional
    public long createPost(String nickname, long memberId, PostRequestDto postRequestDto) {
        Member member = Member.builder()
                .id(memberId)
                .nickname(nickname)
                .build();

        List<MultipartFile> imageFiles = postRequestDto.getImageFiles(); // 이미지 여부 확인용
        String thumbnailUrl = defaultThumbnailImageAddress; // 기본 이미지 주소

        // 썸네일 생성
        if (isPhoto(imageFiles)) {
            thumbnailUrl = createThumbnail(postRequestDto.getImageFiles().get(0));
        }

        // 게시글 생성
        Post post = Post.builder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .nickname(nickname)
                .member(member)
                .thumbnailUrl(thumbnailUrl)
                .build();

        post = postRepository.save(post);

        // dto에 이미지가 있는 경우에만 게시글 맵핑
        if (isPhoto(imageFiles)) { // 이미지가 존재할 경우에만 s3 업로드 및 Photo 객체와 Post 객체 맵핑
            createPhoto(postRequestDto.getImageFiles(), post, member);
        }

        return post.getId();
    }

    // 전체 조회
    @Transactional(readOnly = true)
    public List<PostListResponseDto> getPosts(long memberId, LocalDate firstDay, LocalDate lastDay, int weeks) {
        // 작성자의 memberId가 일치하고, firstDay와 lastDay 사이에 작성된 글을 검색한다.
        var foundPosts = postRepository.findAllByMemberIdAndCreatedDateBetween(memberId, firstDay, lastDay)
                .stream()
                // PostListResponseDto의 배열로 변환한다.
                .map(PostService::toPostListResponseDto)
                .toArray(PostListResponseDto[]::new);

        // 달력 일자의 개수와 같은 크기의 배열 생성
        var posts = new PostListResponseDto[weeks * 7];
        var index = 0;
        for (int i = 0; i < posts.length; i++) {
            // 달력 첫날부터 하루하루 증가
            var today = firstDay.plusDays(i).getDayOfMonth();
            if (index < foundPosts.length) {
                // 게시글의 작성일이 오늘과 일치할 경우 반환할 posts 배열에 대입
                if (foundPosts[index].getCreatedDay() == today) {
                    posts[i] = foundPosts[index];
                    ++index;
                    continue;
                }
            }
            // 작성일이 일치하는 날이 없다면 일기를 쓰지 않은 날이다.
            // 빈 객체를 생성해서 넣어준다.
            posts[i] = PostListResponseDto.builder()
                    .postId(0)
                    .thumbnailUrl(null)
                    .createdDay(today)
                    .build();
        }

        return Arrays.asList(posts);
    }

    // 상세 조회
    @Transactional(readOnly = true)
    public PostResponseDto getPost(long myMemberId, long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException("해당 게시물을 찾을 수 없습니다.")
        );
        long memberId = post.getMemberId();
        if (myMemberId == memberId) {
            return toPostResponseDto(post);
        } else if (friendService.isFriend(myMemberId, memberId)) {
            return toPostResponseDto(post);
        } else {
            throw new IllegalArgumentException("해당 게시물에 접근할 수 없습니다.");
        }
    }

    // 수정
    @Transactional
    public PostResponseDto updatePost(long myMemberId, long postId, PostRequestDto postRequestDto) {

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException("해당 게시물이 존재하지 않습니다."));

        long memberId = post.getMemberId();
        String thumbnailUrl = defaultThumbnailImageAddress;

        if (myMemberId == memberId) {
            if (isPhoto(postRequestDto.getImageFiles())) { // 수정할 이미지가 있는 경우
                if (post.getPhotos().size() == 0) { // post와 맵핑이 된 사진 entity가 없을 경우
                    thumbnailUrl = createThumbnail(postRequestDto.getImageFiles().get(0));
                    createPhoto(postRequestDto.getImageFiles(), post, post.getMember());
                } else { // 0개가 아닌 경우
                    // 기존 썸네일, 사진 삭제
                    s3Service.deleteObjectByImageUrl(post.getThumbnailUrl());
                    deletePhoto(post);

                    // 썸네일 생성, 업로드, post와 맵핑
                    thumbnailUrl = createThumbnail(postRequestDto.getImageFiles().get(0));
                    createPhoto(postRequestDto.getImageFiles(), post, post.getMember());
                }
            } else { // 수정할 이미지가 없는 경우
                if (post.getPhotos().size() != 0) { // 기존 이미지 맵핑이 있는 경우에만
                    // 기존 썸네일, 사진 삭제
                    s3Service.deleteObjectByImageUrl(post.getThumbnailUrl());
                    deletePhoto(post);
                }
            }

            post.update(postRequestDto.getTitle(), postRequestDto.getContent(), thumbnailUrl);


            return toPostResponseDto(post);
        } else {
            throw new IllegalArgumentException("해당 게시물의 수정 권한이 없습니다.");
        }
    }

    // 삭제
    @Transactional
    public String deletePost(long myMemberId, long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException("해당 게시물이 존재하지 않습니다."));

        long memberId = post.getMemberId();
        if (myMemberId == memberId) {
            if (post.getPhotos().size() > 0) { // 맵핑된 사진 게시물이 있는 경우에만 s3에서 삭제
                deletePhoto(post);
                s3Service.deleteObjectByImageUrl(post.getThumbnailUrl());
            }
            postRepository.deleteById(postId);
            return "게시물 삭제 성공";
        } else {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
    }

    // 댓글 목록 보기
    private static List<CommentResponseDto> getCommentList(Post post) {
        if (post.getComments() == null) {
            return new ArrayList<>();
        }

        return post.getComments().stream().map(CommentService::toCommentResponseDto).collect(Collectors.toList());
    }

    private static PostResponseDto toPostResponseDto(Post post) {
        ArrayList<String> urls = new ArrayList<String>();
        // post와 맵핑이 된 photo가 null 인 경우 null이 담긴 배열 달

        if (post.getPhotos().size() == 0) {
            // null 값을 담아 전달
            urls.add(null);
        } else {
            for (var photo : post.getPhotos()) {
                String url = photo.getUrl();
                urls.add(url);
            }
        }

        return PostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .nickname(post.getNickname())
                .memberId(post.getMemberId())
                .images(urls)
                .comments(getCommentList(post))
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .build();
    }

    private static PostListResponseDto toPostListResponseDto(Post post) {
        return PostListResponseDto.builder()
                .postId(post.getId())
                .thumbnailUrl(post.getThumbnailUrl())
                .createdDate(post.getCreatedDate())
                .createdDay(post.getCreatedDate().getDayOfMonth())
                .build();
    }


    private String createThumbnail(MultipartFile multipartFile) {

        // 썸네일 업로드 및 맵핑
        String thumbnailUrl = null;
        try {
            thumbnailUrl = s3Service.uploadThumbnail(multipartFile);
        } catch (IOException e) {
            throw new RuntimeException("썸네일 업로드 실패");
        }

        return thumbnailUrl;
    }

    private void createPhoto(List<MultipartFile> imageFiles, Post post, Member member) {
        // 사진 db 반영
        var photos = new ArrayList<Photo>();
        for (var imageFile : imageFiles) {
            var uploadedImageUrl = s3Service.uploadImage(imageFile);
            Photo photo = Photo.builder()
                    .url(uploadedImageUrl)
                    .post(post)
                    .member((member == null) ? post.getMember() : member)
                    .build();
            photoRepository.save(photo);
            photos.add(photo);
        }
        if (post.getPhotos() != null) {
            post.getPhotos().clear();
            post.getPhotos().addAll(photos);
        }
    }

    private void deletePhoto(Post post) {
        // s3 버킷에서 기존 이미지 삭제
        for (var photo : post.getPhotos()) {
            s3Service.deleteObjectByImageUrl(photo.getUrl());
        }
        post.getPhotos().clear();
    }

    public void validatePostRequest(PostRequestDto postRequestDto, long memberId) {

        if (postRepository.existsByMemberIdAndCreatedDate(memberId, LocalDate.now())) {
            throw new IllegalArgumentException("오늘의 일기를 이미 작성하였습니다.");
        }
    }

    private boolean isPhoto(List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty() || imageFiles.get(0).isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
}
