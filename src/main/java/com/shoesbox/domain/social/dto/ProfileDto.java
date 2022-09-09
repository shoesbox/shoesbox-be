package com.shoesbox.domain.social.dto;

import lombok.*;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Builder
public class ProfileDto {
    private String email;
    private String nickname;
    private String profileImage;
}
