package com.shoesbox.comment;

import lombok.Getter;

@Getter
public class CommentResponseDto {
    private String nickname;
    private String content;

    public CommentResponseDto(String nickname, String content){
        this.nickname = nickname;
        this.content = content;
    }
}
