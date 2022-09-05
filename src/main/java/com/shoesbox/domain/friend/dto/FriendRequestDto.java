package com.shoesbox.domain.friend.dto;

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
public class FriendRequestDto {
    @NotBlank(message = "email이 공란입니다")
    private String email;
}
