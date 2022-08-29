package com.shoesbox.comment;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
public class CommentRequestDto {
    private Long userId;
    @NotBlank(message = "server : 닉네임 입력하지 않음")
    private String username;
    @NotBlank(message = "server : 내용 입력하지 않음")
    private String content;

    public CommentRequestDto(Long userId, String username, String content){
        this.userId = userId;
        this.username = username;
        this.content = content;
    }
}