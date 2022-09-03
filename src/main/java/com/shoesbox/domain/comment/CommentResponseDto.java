package com.shoesbox.domain.comment;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CommentResponseDto {
    Long commentId;
    String nickname;
    String content;
    Long postId;
    Long memberId;

    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getId();
        this.nickname = comment.getNickname();
        this.content = comment.getContent();
        this.postId = comment.getPostId();
        this.memberId = comment.getMemberId();
    }
}
