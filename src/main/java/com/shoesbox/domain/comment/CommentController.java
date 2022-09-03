package com.shoesbox.domain.comment;

import com.shoesbox.global.common.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/api/comments")
@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{postId}")
    public ResponseEntity<Object> readComment(@PathVariable Long postId) {
        return ResponseHandler.ok(commentService.readComment(postId));
    }

    @PostMapping("/{postId}")
    public ResponseEntity<Object> createComment(@PathVariable Long postId,
                                                @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return ResponseHandler.ok(commentService.createComment(postId, commentRequestDto));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Object> updateComment(@PathVariable("commentId") Long commentId,
                                                @Valid @RequestBody CommentRequestDto commentRequestDto) {
        return ResponseHandler.ok(commentService.updateComment(commentId, commentRequestDto));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Object> deleteComment(@PathVariable("commentId") Long commentId) {
        return ResponseHandler.ok(commentService.deleteComment(commentId));
    }
}
