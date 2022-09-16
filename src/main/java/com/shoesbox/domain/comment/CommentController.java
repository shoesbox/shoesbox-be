package com.shoesbox.domain.comment;

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
    public ResponseEntity<Object> readComment(@PathVariable long postId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(commentService.readComment(postId, currentMemberId));
    }

    @PostMapping("/{postId}")
    public ResponseEntity<Object> createComment(@PathVariable long postId,
                                                @Valid @RequestBody CommentRequestDto commentRequestDto) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        String currentMemberNickname = SecurityUtil.getCurrentMemberNickname();
        return ResponseHandler.ok(commentService.createComment(
                currentMemberNickname, commentRequestDto.getContent(), currentMemberId, postId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Object> updateComment(@PathVariable("commentId") long commentId,
                                                @Valid @RequestBody CommentRequestDto commentRequestDto) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(commentService.updateComment(currentMemberId, commentId, commentRequestDto));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Object> deleteComment(@PathVariable("commentId") long commentId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(commentService.deleteComment(currentMemberId, commentId));
    }
}
