package com.shoesbox.post;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    public Post createPost(@RequestBody PostRequestDto postRequestDto){
        return this.postService.createPost(postRequestDto);
    }

}