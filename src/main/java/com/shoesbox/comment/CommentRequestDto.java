package com.shoesbox.comment;

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
public class CommentRequestDto {
    private Long memberId;
    @NotBlank(message = "server : 닉네임 입력하지 않음")
    private String nickname;
    @NotBlank(message = "server : 내용 입력하지 않음")
    private String content;

    public CommentRequestDto(Long memberId, String nickname, String content){
        this.memberId = memberId;
        this.nickname = nickname;
        this.content = content;
    }
}