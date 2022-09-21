package com.shoesbox.domain.comment;

import com.shoesbox.domain.comment.dto.CommentRequestDto;
import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.util.SecurityUtil;
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
    public ResponseEntity<Object> readComments(@PathVariable long postId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(commentService.readComments(postId, currentMemberId));
    }

    @PostMapping("/{postId}")
    public ResponseEntity<Object> createComment(
            @PathVariable long postId, @Valid @RequestBody CommentRequestDto commentRequestDto) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(commentService.createComment(
                commentRequestDto.getContent(), currentMemberId, postId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Object> updateComment(
            @PathVariable("commentId") long commentId, @Valid @RequestBody CommentRequestDto commentRequestDto) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(commentService.updateComment(currentMemberId, commentId, commentRequestDto));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Object> deleteComment(@PathVariable("commentId") long commentId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(commentService.deleteComment(currentMemberId, commentId));
    }
}
