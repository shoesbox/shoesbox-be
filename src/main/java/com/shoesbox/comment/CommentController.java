package com.shoesbox.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/api/comments")
@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/posts/{postId}")
    public List<CommentResponseDto> readComment(@PathVariable Long postId){
        return commentService.readComment(postId);
    }

    @PostMapping("/{postId}")
    public CommentRequestDto createComment(@PathVariable Long postId, @Valid @RequestBody CommentRequestDto commentRequestDto){
        return commentService.createComment(postId, commentRequestDto);
    }

    @PutMapping("/{commentId}")
    public String updateComment(@PathVariable("commentId") Long commentId, @Valid @RequestBody CommentRequestDto commentRequestDto){
        return commentService.updateComment(commentId, commentRequestDto);
    }

    @DeleteMapping("/{commentId}")
    public String deleteComment(@PathVariable("commentId") Long commentId){
        return commentService.deleteComment(commentId);
    }
}