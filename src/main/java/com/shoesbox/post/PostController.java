package com.shoesbox.post;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    private final PostRepository postRepository;

    @PostMapping
    public String createPost(@RequestBody PostRequestDto postRequestDto){
        this.postService.createPost(postRequestDto);
        return "redirect:/post";
    }

}