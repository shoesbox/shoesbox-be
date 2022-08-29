package com.shoesbox.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/api/posts")
@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/{postId}/comments")
    public CommentRequestDto createComment(@PathVariable Long postId, @Valid @RequestBody CommentRequestDto commentRequestDto){
        return commentService.createComment(postId, commentRequestDto);
    }
}