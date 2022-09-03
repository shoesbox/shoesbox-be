package com.shoesbox.domain.post;

import com.shoesbox.domain.comment.Comment;
import com.shoesbox.domain.comment.CommentResponseDto;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.post.dto.PostListResponseDto;
import com.shoesbox.domain.post.dto.PostRequestDto;
import com.shoesbox.domain.post.dto.PostResponseDto;
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

    // 전체 조회
    public Page<PostListResponseDto> getPostList(Pageable pageable) {
        return postRepository.findAll(pageable).map(PostService::toPostListResponseDto);
    }

    // 생성
    @Transactional
    public PostResponseDto createPost(String nickname, long memberId, PostRequestDto postRequestDto) {
        Member member = Member.builder()
                .id(memberId)
                .build();
        Post post = Post.builder()
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .author(nickname)
                .member(member)
                .build();
        postRepository.save(post);
        return toPostResponseDto(post);
    }

    // 댓글 목록 보기
    private static List<CommentResponseDto> getCommentList(Post post) {
        if (post.getComments() == null) {
            return new ArrayList<>();
        }

        List<CommentResponseDto> commentList = new ArrayList<>();
        for (Comment comment : post.getComments()) {
            commentList.add(new CommentResponseDto(comment));
        }
        return commentList;
    }

    private static PostResponseDto toPostResponseDto(Post post) {
        return PostResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor())
                .comments(getCommentList(post))
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .createdYear(post.getCreatedYear())
                .createdMonth(post.getCreatedMonth())
                .createdDay(post.getCreatedDay())
                .build();
    }

    private static PostListResponseDto toPostListResponseDto(Post post) {
        return PostListResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .thumbnailUrl("URL")
                .createdAt(post.getCreatedAt())
                .modifiedAt(post.getModifiedAt())
                .createdYear(post.getCreatedYear())
                .createdMonth(post.getCreatedMonth())
                .createdDay(post.getCreatedDay())
                .build();
    }
}
