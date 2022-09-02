package com.shoesbox.post;

import com.shoesbox.comment.Comment;
import com.shoesbox.comment.CommentResponseDto;
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
                    .comment(getCommentList(post))
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

    private List<CommentResponseDto> getCommentList(Post post){
        List<CommentResponseDto> commentList = new ArrayList<>();
        List<Comment> comments = post.getComments();

        for(Comment comment:comments){
            CommentResponseDto commentResponseDto = CommentResponseDto.builder().comment(comment).build();
            commentList.add(commentResponseDto);
        }
        return commentList;
    }
}
