package com.shoesbox.comment;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentResponseDto {
    private Long commentId;
    private String nickname;
    private String content;
    private Long postId;
    private Long memberId;

    @Builder
    public CommentResponseDto(Comment comment){
        this.commentId = comment.getId();
        this.nickname = comment.getNickname();
        this.content = comment.getContent();
        this.postId = comment.getPost().getId();
        this.memberId = comment.getMemberId();
    }
}