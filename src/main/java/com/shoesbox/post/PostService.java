package com.shoesbox.post;

import com.shoesbox.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final CommentService commentService;

    // 전체 조회
    public List<PostResponseDto> getPostList() {
        List<Post> posts = postRepository.findAll();
        List<PostResponseDto> postList = new ArrayList<>();

        for (Post post : posts) {
            PostResponseDto postResponseDto = PostResponseDto.builder()
                    .post(post)
                    .comment(commentService.getCommentList(post.getId()))
                    .build();
            postList.add(postResponseDto);
        }

        return postList;
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
