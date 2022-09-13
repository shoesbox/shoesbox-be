package com.shoesbox.domain.post;

import com.shoesbox.domain.friend.FriendService;
import com.shoesbox.domain.post.dto.PostRequestDto;
import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final FriendService friendService;

    // 생성
    @PostMapping
    public ResponseEntity<Object> createPost(PostRequestDto postRequestDto) {
        long memberId = SecurityUtil.getCurrentMemberId();
        String nickname = SecurityUtil.getCurrentMemberNickname();
        return ResponseHandler.ok(postService.createPost(nickname, memberId, postRequestDto));
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<Object> getPosts(
            @PageableDefault(size = 31, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "id", defaultValue = "0", required = false) long memberId,
            @RequestParam(value = "y", defaultValue = "0", required = false) int year,
            @RequestParam(value = "m", defaultValue = "0", required = false) int month) {

        long currentMemberId = SecurityUtil.getCurrentMemberId();

        if (memberId == 0 || memberId == currentMemberId) {
            memberId = currentMemberId;
        } else {
            if(!friendService.isFriend(memberId, currentMemberId)){
                throw new IllegalArgumentException("해당 회원과 친구 상태가 아닙니다.");
            }
        }

        if (year == 0) {
            year = LocalDate.now().getYear();
        }

        if (month == 0) {
            month = LocalDate.now().getMonthValue();
        }

        return ResponseHandler.ok(postService.getPosts(pageable, memberId, year, month));
    }

    // 상세 조회
    @GetMapping("/{postId}")
    public ResponseEntity<Object> getPost(@PathVariable long postId) {
        long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(postService.getPost(memberId, postId));
    }

    // 수정
    @PutMapping("/{postId}")
    public ResponseEntity<Object> updatePost(@PathVariable long postId, @RequestBody PostRequestDto postRequestDto) {
        long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(postService.updatePost(memberId, postId, postRequestDto));
    }

    // 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Object> deletePost(@PathVariable long postId) {
        long memberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(postService.deletePost(memberId, postId));
    }

}
