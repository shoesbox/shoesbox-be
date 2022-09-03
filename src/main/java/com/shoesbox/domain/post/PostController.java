package com.shoesbox.domain.post;

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

    @PostMapping
    public ResponseEntity<Object> createPost(@RequestBody PostRequestDto postRequestDto) {
        long memberId = SecurityUtil.getCurrentMemberIdByLong();
        String nickname = SecurityUtil.getCurrentMemberNickname();
        return ResponseHandler.ok(postService.createPost(nickname, memberId, postRequestDto));
    }

    @GetMapping
    public ResponseEntity<Object> getAllPost(
            @PageableDefault(size = 31, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "id", defaultValue = "0", required = false) long memberId,
            @RequestParam(value = "y", defaultValue = "0", required = false) int year,
            @RequestParam(value = "m", defaultValue = "0", required = false) int month) {
        if (memberId == 0) {
            memberId = SecurityUtil.getCurrentMemberIdByLong();
        }

        if (year == 0) {
            year = LocalDate.now().getYear();
        }

        if (month == 0) {
            month = LocalDate.now().getMonthValue();
        }

        return ResponseHandler.ok(postService.getPostList(pageable, memberId, year, month));
    }

}
