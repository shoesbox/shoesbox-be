package com.shoesbox.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public List<CommentResponseDto> readComment(Long postId){
        List<CommentResponseDto> commentList = new ArrayList<>();

        List<Comment> comments = commentRepository.findAllByPostId(postId);
        for(Comment comment : comments){
            CommentResponseDto commentResponseDto = new CommentResponseDto(comment.getUsername(), comment.getContent());
            commentList.add(commentResponseDto);
        }
        return commentList;
    }

    public CommentRequestDto createComment(Long postId, CommentRequestDto commentRequestDto){
        Comment comment = new Comment(postId, commentRequestDto);
        commentRepository.save(comment);

        return commentRequestDto;
    }
}
