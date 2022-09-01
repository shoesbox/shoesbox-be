package com.shoesbox.comment;

import com.shoesbox.global.common.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RequestMapping("/api/comments")
@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{postId}")
    public ResponseEntity<ResponseWrapper<List<CommentResponseDto>>> readComment(@PathVariable Long postId){
        return ResponseWrapper.ok(commentService.readComment(postId));
    }

    @PostMapping("/{postId}")
    public ResponseEntity<ResponseWrapper<CommentResponseDto>> createComment(@PathVariable Long postId,
                                                 @Valid @RequestBody CommentRequestDto commentRequestDto){
        return ResponseWrapper.ok(commentService.createComment(postId, commentRequestDto));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ResponseWrapper<CommentResponseDto>> updateComment(@PathVariable("commentId") Long commentId,
                                                                            @Valid @RequestBody CommentRequestDto commentRequestDto){
        return ResponseWrapper.ok(commentService.updateComment(commentId, commentRequestDto));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseWrapper<String>> deleteComment(@PathVariable("commentId") Long commentId){
        return ResponseWrapper.ok(commentService.deleteComment(commentId));
    }
}