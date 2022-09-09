package com.shoesbox.domain.post;

import com.shoesbox.domain.comment.Comment;
import com.shoesbox.domain.comment.CommentResponseDto;
import com.shoesbox.domain.comment.CommentService;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.photo.Photo;
import com.shoesbox.domain.photo.PhotoRepository;
import com.shoesbox.domain.photo.S3Service;
import com.shoesbox.domain.post.dto.PostListResponseDto;
import com.shoesbox.domain.post.dto.PostRequestDto;
import com.shoesbox.domain.post.dto.PostResponseDto;
import com.shoesbox.global.exception.runtime.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final PhotoRepository photoRepository;
    private final S3Service s3Service;

    // 생성
    @Transactional
    public long createPost(String nickname, long memberId, PostRequestDto postRequestDto) {
        if (postRequestDto.getImageFiles() == null || postRequestDto.getImageFiles().isEmpty()) {
            throw new IllegalArgumentException("이미지를 최소 1장 이상 첨부해야 합니다.");
        }

        Member member = Member.builder()
                .id(memberId)
                .build();

        // 게시글 생성
        Post post = Post.builder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .author(nickname)
                .member(member)
                .build();
        post = postRepository.save(post);

        // 이미지 업로드
        if (postRequestDto.getImageFiles() != null) {
            for (var imageFile : postRequestDto.getImageFiles()) {
                if (imageFile.isEmpty()) {
                    continue;
                }
                var uploadedImageUrl = s3Service.uploadImage(imageFile);
                Photo photo = Photo.builder()
                        .url(uploadedImageUrl)
                        .post(post)
                        .member(member)
                        .build();
                photoRepository.save(
                        photo
                );
            }
        }

        return post.getId();
    }

    // 전체 조회
    @Transactional(readOnly = true)
    public Page<PostListResponseDto> getPosts(Pageable pageable, long memberId, int year, int month) {
        return postRepository.findByMemberIdAndCreatedYearAndCreatedMonth(pageable, memberId, year, month).map(PostService::toPostListResponseDto);
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
        } else {
            throw new IllegalArgumentException("해당 게시물에 접근할 수 없습니다.");
        }
    }

    // 수정
    @Transactional
    public PostResponseDto updatePost(long myMemberId, long postId, PostRequestDto postRequestDto) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException("해당 게시물이 존재하지 않습니다.")
        );
        long memberId = post.getMemberId();
        if (myMemberId == memberId) {
            post.update(postRequestDto.getTitle(), postRequestDto.getContent());//, postRequestDto.getImages());
            postRepository.save(post);
            return toPostResponseDto(post);
        } else {
            throw new IllegalArgumentException("해당 게시물의 수정 권한이 없습니다.");
        }
    }

    // 삭제
    @Transactional
    public String deletePost(long myMemberId, long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException("해당 게시물이 존재하지 않습니다.")
        );
        long memberId = post.getMemberId();
        if (myMemberId == memberId) {
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

        List<CommentResponseDto> commentList = new ArrayList<>();
        for (Comment comment : post.getComments()) {
            commentList.add(CommentService.toCommentResponseDto(comment, post.getMemberId(), post.getId()));
        }
        return commentList;
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
                .nickname(post.getAuthor())
                .memberId(post.getMemberId())
                .url(urls)
                .comments(getCommentList(post))
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .createdYear(post.getCreatedYear())
                .createdMonth(post.getCreatedMonth())
                .createdDay(post.getCreatedDay())
                .build();
    }

    private static PostListResponseDto toPostListResponseDto(Post post) {
        String url = null;
        if (post.getPhotos() != null && !post.getPhotos().isEmpty()) {
            url = post.getPhotos().get(0).getUrl();
        }
        return PostListResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                // TODO: 썸네일 최적화 필요
                .thumbnailUrl(url)
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .createdYear(post.getCreatedYear())
                .createdMonth(post.getCreatedMonth())
                .createdDay(post.getCreatedDay())
                .build();
    }
}
