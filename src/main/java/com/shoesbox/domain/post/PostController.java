package com.shoesbox.domain.post;

import com.shoesbox.domain.guest.GuestService;
import com.shoesbox.domain.post.dto.PostRequestDto;
import com.shoesbox.domain.post.dto.PostUpdateDto;
import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final GuestService guestService;

    // 생성
    @PostMapping
    public ResponseEntity<Object> createPost(PostRequestDto postRequestDto) {
//        response.setContentType("text/event-stream");
//        response.setCharacterEncoding("UTF-8");
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        guestService.guestCheck(currentMemberId);
        return ResponseHandler.ok(postService.createPost(currentMemberId, postRequestDto));
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<Object> getPosts(
            @RequestParam(value = "id", defaultValue = "0", required = false) long targetId,
            @RequestParam(value = "y", defaultValue = "0", required = false) int year,
            @RequestParam(value = "m", defaultValue = "0", required = false) int month) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (targetId == 0) {
            targetId = currentMemberId;
        }
        if (year == 0) {
            year = LocalDate.now().getYear();
        }
        if (month == 0) {
            month = LocalDate.now().getMonthValue();
        }
        return ResponseHandler.ok(postService.getPosts(currentMemberId, targetId, year, month));
    }

    // 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<Object> getPost(@PathVariable long postId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(postService.getPost(currentMemberId, postId));
    }

    // 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<Object> updatePost(@PathVariable long postId, PostUpdateDto postUpdateDto) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        guestService.guestCheck(currentMemberId);
        return ResponseHandler.ok(postService.updatePost(currentMemberId, postId, postUpdateDto));
    }

    // 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Object> deletePost(@PathVariable long postId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        guestService.guestCheck(currentMemberId);
        return ResponseHandler.ok(postService.deletePost(currentMemberId, postId));
    }

    // 강제삭제
    @DeleteMapping("/admin/{postId}")
    public ResponseEntity<Object> deletePostAdmin(@PathVariable long postId) {
        return ResponseHandler.ok(postService.deletePostAdmin(postId));
    }
}
