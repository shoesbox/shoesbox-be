package com.shoesbox.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;

    // 전체 조회
    public List<PostResponseDto> getPostList() {
        List<Post> posts = postRepository.findAll();
        List<PostResponseDto> postList = new ArrayList<>();

        for (Post post : posts) {
            PostResponseDto postResponseDto = PostResponseDto.builder()
                    .post(post)
                    .build();
            postList.add(postResponseDto);
        }

        return postList;
    }

    // 상세 조회
    public PostResponseDto getPost(Long post_id) {
        Post post = postRepository.findById(post_id).orElseThrow(
                () -> new NullPointerException()
        );
        PostResponseDto postResponseDto = PostResponseDto.builder()
                .post(post)
                .build();
        return postResponseDto;
    }

    // 생성
    @Transactional
    public Post createPost(PostRequestDto dto) {
        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .images(dto.getImages())
                .build();
        postRepository.save(post);
        return post;
    }
}
