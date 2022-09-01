package com.shoesbox.comment;

import com.shoesbox.post.Post;
import com.shoesbox.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public List<CommentResponseDto> readComment(Long postId){
        return getCommentList(postId);
    }

    @Transactional
    public CommentResponseDto createComment(Long postId, CommentRequestDto commentRequestDto){
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        Comment comment = new Comment(commentRequestDto, post);
        commentRepository.save(comment);

        CommentResponseDto commentResponseDto = CommentResponseDto.builder().comment(comment).build();

        return commentResponseDto;
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto commentRequestDto){
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        comment.update(commentRequestDto);

        // 수정 확인용
        comment = commentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));
        CommentResponseDto commentResponseDto = CommentResponseDto.builder().comment(comment).build();
        return commentResponseDto;
    }

    @Transactional
    public String deleteComment(Long commentId){
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        commentRepository.delete(comment);
        return "댓글 삭제 성공";
    }

    public List<CommentResponseDto> getCommentList(Long postId){
        List<CommentResponseDto> commentList = new ArrayList<>();
        List<Comment> comments = commentRepository.findAllByPostId(postId);

        for(Comment comment:comments){
            CommentResponseDto commentResponseDto = CommentResponseDto.builder().comment(comment).build();
            commentList.add(commentResponseDto);
        }
        return commentList;
    }
}