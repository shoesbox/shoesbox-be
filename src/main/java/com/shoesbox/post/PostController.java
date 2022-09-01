package com.shoesbox.post;

import com.shoesbox.global.common.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<ResponseWrapper<PostResponseDto>> createPost(@RequestBody PostRequestDto postRequestDto) {
        return ResponseWrapper.ok(postService.createPost(postRequestDto));
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<PostResponseDto>>> getAllPost() {
        return ResponseWrapper.ok(postService.getPostList());
    }

    @GetMapping("/{post_id}")
    public ResponseEntity<ResponseWrapper<PostResponseDto>> getPost(@PathVariable Long post_id) {
        return ResponseWrapper.ok(postService.getPost(post_id));
    }

    @PutMapping("/{post_id}")
    public ResponseEntity<ResponseWrapper<PostResponseDto>> updatePost(@PathVariable Long post_id, @RequestBody PostRequestDto postRequestDto) {
        return ResponseWrapper.ok(postService.updatePost(post_id, postRequestDto));
    }

    @DeleteMapping("/{post_id}")
    public ResponseEntity<ResponseWrapper<String>> deletePost(@PathVariable Long post_id) {
        return ResponseWrapper.ok(postService.deletePost(post_id));
    }
}