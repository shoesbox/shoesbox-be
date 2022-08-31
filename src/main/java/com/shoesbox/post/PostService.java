package com.shoesbox.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;

    @Transactional
    public Post createPost(PostRequestDto postRequestDto){
        Post post = Post.builder()
                .build();
        postRepository.save(post);
        return post;
    }
}
