package com.shoesbox.post;

import com.shoesbox.global.exception.runtime.PostNotFoundException;
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
                () -> new PostNotFoundException("해당 게시물을 찾을 수 없습니다.")
        );
        return PostResponseDto.builder()
                .post(post)
                .build();
    }

    // 생성
    @Transactional
    public PostResponseDto createPost(PostRequestDto dto) {
        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .images(dto.getImages())
                .build();
        postRepository.save(post);
        return PostResponseDto.builder().post(post).build();
    }

    @Transactional
    public PostResponseDto updatePost(Long post_id, PostRequestDto postRequestDto) {
        Post post = postRepository.findById(post_id).orElseThrow(
                () -> new PostNotFoundException("수정하려는 해당 게시물이 존재하지 않습니다.")
        );
        post.update(postRequestDto.getTitle(), postRequestDto.getContent(), postRequestDto.getImages());
        postRepository.save(post);
        return PostResponseDto.builder().post(post).build();
    }

    @Transactional
    public String deletePost(Long post_id) {
        Post post = postRepository.findById(post_id).orElseThrow(
                () -> new PostNotFoundException("삭제하려는 해당 게시물이 존재하지 않습니다.")
        );
        postRepository.deleteById(post_id);
        return "게시물 삭제 성공";

    }
}
