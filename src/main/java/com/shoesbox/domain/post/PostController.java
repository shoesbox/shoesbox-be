package com.shoesbox.domain.post;

import com.shoesbox.domain.post.dto.PostRequestDto;
import com.shoesbox.domain.post.dto.PostResponseDto;
import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @PageableDefault(size = 31, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseHandler.ok(postService.getPostList(pageable));
    }

}
