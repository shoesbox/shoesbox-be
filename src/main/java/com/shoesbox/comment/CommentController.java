package com.shoesbox.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/api/posts")
@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{postId}/comments")
    public List<CommentResponseDto> readComment(@PathVariable Long postId){
        return commentService.readComment(postId);
    }

    @PostMapping("/{postId}/comments")
    public CommentRequestDto createComment(@PathVariable Long postId, @Valid @RequestBody CommentRequestDto commentRequestDto){
        return commentService.createComment(postId, commentRequestDto);
    }
}