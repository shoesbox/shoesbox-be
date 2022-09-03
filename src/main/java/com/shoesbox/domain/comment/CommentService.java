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
            commentList.add(new CommentResponseDto(comment));
        }

        return commentList;
    }

    @Transactional
    public CommentResponseDto createComment(long currentMemberId, long postId, CommentRequestDto commentRequestDto) {
        Member member = Member.builder()
                .id(currentMemberId)
                .build();
        Post post = new Post();
        post.setId(postId);
        Comment comment = new Comment(commentRequestDto, member, post);
        commentRepository.save(comment);

        return toCommentResponseDto(comment);
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

        return toCommentResponseDto(comment);
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

    private static CommentResponseDto toCommentResponseDto(Comment comment) {
        return new CommentResponseDto(comment);
    }
}
