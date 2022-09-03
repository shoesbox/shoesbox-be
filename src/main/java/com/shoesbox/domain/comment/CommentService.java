package com.shoesbox.domain.comment;

import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.post.Post;
import com.shoesbox.domain.post.PostRepository;
import com.shoesbox.global.exception.runtime.PostNotFoundException;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<CommentResponseDto> readComment(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(
                        () -> new PostNotFoundException("해당 게시글이 존재하지 않습니다."));

        var comments = post.getComments();
        List<CommentResponseDto> commentList = new ArrayList<>();
        for (Comment comment : comments) {
            commentList.add(toCommentResponseDto(comment, post.getMemberId(), post.getId()));
        }

        return commentList;
    }

    @Transactional
    public CommentResponseDto createComment(
            String currentMemberNickname,
            String content,
            long currentMemberId,
            long postId) {
        Member member = Member.builder()
                .id(currentMemberId)
                .build();

        Post post = Post.builder()
                .id(postId)
                .member(member)
                .build();

        Comment comment = Comment.builder()
                .nickname(currentMemberNickname)
                .content(content)
                .member(member)
                .post(post)
                .build();
        commentRepository.save(comment);

        return toCommentResponseDto(comment, currentMemberId, postId);
    }

    @Transactional
    public CommentResponseDto updateComment(long currentMemberId, long commentId, CommentRequestDto commentRequestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(
                        () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        Post post = comment.getPost();
        if (post.getMemberId() != currentMemberId) {
            throw new UnAuthorizedException("본인이 작성한 댓글만 수정 가능합니다.");
        }

        comment.update(commentRequestDto);

        return toCommentResponseDto(comment, currentMemberId, comment.getPostId());
    }

    @Transactional
    public String deleteComment(long currentMemberId, long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        Post post = comment.getPost();
        if (post.getMemberId() != currentMemberId) {
            throw new UnAuthorizedException("본인이 작성한 댓글만 삭제 가능합니다.");
        }

        commentRepository.delete(comment);
        return "댓글 삭제 성공";
    }

    public static CommentResponseDto toCommentResponseDto(Comment comment, long memberId, long postId) {
        return CommentResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .nickname(comment.getNickname())
                .memberId(memberId)
                .postId(postId)
                .createdAt(comment.getCreatedAt())
                .modifiedAt(comment.getModifiedAt())
                .build();
    }
}
