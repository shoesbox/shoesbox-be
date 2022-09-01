package com.shoesbox.post;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    public PostResponseDto createPost(@RequestBody PostRequestDto postRequestDto) {
        return this.postService.createPost(postRequestDto);
    }

    @GetMapping
    public List<PostResponseDto> getAllPost() {
        return this.postService.getPostList();
    }

    @GetMapping("/{post_id}")
    public PostResponseDto getPost(@PathVariable Long post_id) {
        return postService.getPost(post_id);
    }

    @PutMapping("/{post_id}")
    public PostResponseDto updatePost(@PathVariable Long post_id, @RequestBody PostRequestDto postRequestDto) {
        return this.postService.updatePost(post_id, postRequestDto);
    }

    @DeleteMapping("/{post_id}")
    public PostResponseDto deletePost(@PathVariable Long post_id) {
        return this.postService.deletePost(post_id);
    }
}