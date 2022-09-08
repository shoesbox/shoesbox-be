package com.shoesbox.domain.member.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class MemberInfoResponseDto {
    String nickname;
    String email;
    String profileImageUrl;
    String selfDescription;
}
