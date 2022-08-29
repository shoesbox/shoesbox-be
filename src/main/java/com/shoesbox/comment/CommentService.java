package com.shoesbox.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentRequestDto createComment(Long postId, CommentRequestDto commentRequestDto){
        Comment comment = new Comment(postId, commentRequestDto);
        commentRepository.save(comment);

        return commentRequestDto;
    }
}
