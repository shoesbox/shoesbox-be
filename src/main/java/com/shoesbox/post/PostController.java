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
    public Post createPost(@RequestBody PostRequestDto postRequestDto) {
        return this.postService.createPost(postRequestDto);
    }

    @GetMapping
    public List<PostResponseDto> getAllPost() {
        return this.postService.getPostList();
    }

}