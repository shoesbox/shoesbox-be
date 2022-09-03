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
        return ResponseHandler.ok(commentService.readComment(postId));
    }

    @PostMapping("/{postId}")
    public ResponseEntity<Object> createComment(@PathVariable long postId,
                                                @Valid @RequestBody CommentRequestDto commentRequestDto) {
        long currentMemberId = SecurityUtil.getCurrentMemberIdByLong();
        return ResponseHandler.ok(commentService.createComment(currentMemberId, postId, commentRequestDto));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Object> updateComment(@PathVariable("commentId") long commentId,
                                                @Valid @RequestBody CommentRequestDto commentRequestDto) {
        long memberId = SecurityUtil.getCurrentMemberIdByLong();
        return ResponseHandler.ok(commentService.updateComment(memberId, commentId, commentRequestDto));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Object> deleteComment(@PathVariable("commentId") long commentId) {
        long memberId = SecurityUtil.getCurrentMemberIdByLong();
        return ResponseHandler.ok(commentService.deleteComment(memberId, commentId));
    }
}
