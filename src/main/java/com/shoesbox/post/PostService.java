package com.shoesbox.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;

    @Transactional
    public Post createPost(PostRequestDto dto){
        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .is_private(dto.is_private())
                .year(dto.getYear())
                .month(dto.getMonth())
                .day(dto.getDay())
                .build();
        postRepository.save(post);
        return post;
    }
}
