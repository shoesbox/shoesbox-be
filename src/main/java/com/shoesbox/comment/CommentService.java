package com.shoesbox.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    @Transactional
    public List<Comment> readComment(Long postId){
        return commentRepository.findAllByPostId(postId);
    }

    @Transactional
    public CommentRequestDto createComment(Long postId, CommentRequestDto commentRequestDto){
        Comment comment = new Comment(postId, commentRequestDto);
        commentRepository.save(comment);

        return commentRequestDto;
    }

    @Transactional
    public String updateComment(Long postId, Long commentId, CommentRequestDto commentRequestDto){
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글 혹은 댓글이 존재하지 않음"));

        comment.update(commentRequestDto);

        return "댓글 수정 성공";
    }

    @Transactional
    public String deleteComment(Long postId, Long commentId){
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글 혹은 댓글이 존재하지 않음"));

        commentRepository.delete(comment);
        return "댓글 삭제 성공";
    }
}