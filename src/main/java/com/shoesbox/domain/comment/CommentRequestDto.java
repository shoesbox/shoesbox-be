package com.shoesbox.domain.comment;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;

@Jacksonized
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CommentRequestDto {
    long memberId;
    @NotBlank(message = "닉네임 입력하지 않음")
    String nickname;
    @NotBlank(message = "내용 입력하지 않음")
    String content;
}
