package com.shoesbox.comment;

import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
public class CommentRequestDto {
    private Long memberId;
    @NotBlank(message = "닉네임 입력하지 않음")
    private String nickname;
    @NotBlank(message = "내용 입력하지 않음")
    private String content;

    public CommentRequestDto(Long memberId, String nickname, String content){
        this.memberId = memberId;
        this.nickname = nickname;
        this.content = content;
    }
}