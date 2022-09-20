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

        String thumbnailUrl = createThumbnail(postRequestDto.getImageFiles().get(0));

        // 게시글 생성
        Post post = Post.builder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .nickname(nickname)
                .member(member)
                .thumbnailUrl(thumbnailUrl)
                .build();
        post = postRepository.save(post);

        // 이미지 업로드
        createPhoto(postRequestDto.getImageFiles(), post, member);

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
    public PostResponseDto updatePost(long currentMemberId, long postId, PostRequestDto postRequestDto) {
        Post post = getPost(postId);

        long memberId = post.getMemberId();
        if (currentMemberId != memberId) {
            throw new IllegalArgumentException("해당 게시물의 수정 권한이 없습니다.");
        }
        // 첨부 이미지를 수정하는지 검사
        try {
            validateImageFiles(postRequestDto.getImageFiles());
        } catch (IllegalArgumentException e) {
            // 첨부 이미지가 없으면 기존 썸네일 재활용
            post.update(postRequestDto.getTitle(), postRequestDto.getContent(), post.getThumbnailUrl());
            return toPostResponseDto(post);
        }

        // 기존 썸네일, 사진 삭제
        s3Service.deleteObjectByImageUrl(post.getThumbnailUrl());
        deletePhotosInPost(post);

        // 썸네일 생성
        String thumbnailUrl = createThumbnail(postRequestDto.getImageFiles().get(0));
        createPhoto(postRequestDto.getImageFiles(), post);
        post.update(postRequestDto.getTitle(), postRequestDto.getContent(), thumbnailUrl);
        return toPostResponseDto(post);
    }

    // 삭제
    @Transactional
    public long deletePost(long myMemberId, long postId) {
        Post post = getPost(postId);

        long memberId = post.getMemberId();
        if (myMemberId != memberId) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        deletePhotosInPost(post);
        s3Service.deleteObjectByImageUrl(post.getThumbnailUrl());
        postRepository.deleteById(postId);
        return postId;
    }

    private Post getPost(long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("해당 게시물이 존재하지 않습니다."));
    }

    private PostListResponseDto[] getPostsByDate(long memberId, LocalDate firstDay, LocalDate lastDay) {
        return postRepository.findAllByMemberIdAndCreatedDateBetween(memberId, firstDay, lastDay)
                             .stream()
                             // PostListResponseDto의 배열로 변환한다.
                             .map(this::toPostListResponseDto)
                             .toArray(PostListResponseDto[]::new);
    }

    // 댓글 목록 보기
    private static List<CommentResponseDto> getCommentList(Post post) {
        if (post.getComments() == null) {
            return new ArrayList<>();
        }

        return post.getComments().stream().map(CommentService::toCommentResponseDto).collect(Collectors.toList());
    }

    private static PostResponseDto toPostResponseDto(Post post) {
        var urls = new ArrayList<String>();
        for (var photo : post.getPhotos()) {
            urls.add(photo.getUrl());
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
            // TODO: 2022-09-16 기본 썸네일로 대체해 업로드
            throw new RuntimeException("썸네일을 생성할 수 없습니다.");
        }

        return thumbnailUrl;
    }

    private void createPhoto(List<MultipartFile> imageFiles, Post post, Member member) {
        if (imageFiles == null || imageFiles.isEmpty() || imageFiles.get(0).isEmpty()) {
            throw new IllegalArgumentException("이미지를 최소 1장 이상 첨부해야 합니다.");
        }

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

    private void deletePhotosInPost(Post post) {
        if (post.getPhotos() != null) {
            // s3 버킷에서 기존 이미지 삭제
            for (var photo : post.getPhotos()) {
                s3Service.deleteObjectByImageUrl(photo.getUrl());
            }
            post.getPhotos().clear();
        }
    }

    public void validatePostRequest(PostRequestDto postRequestDto, long memberId) {
        validateImageFiles(postRequestDto.getImageFiles());

        if (postRepository.existsByMemberIdAndCreatedDate(memberId, LocalDate.now())) {
            throw new IllegalArgumentException("오늘의 일기를 이미 작성하였습니다.");
        }
    }

    private void validateImageFiles(List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty() || imageFiles.get(0).isEmpty()) {
            throw new IllegalArgumentException("이미지를 최소 1장 이상 첨부해야 합니다.");
        }
    }
}
