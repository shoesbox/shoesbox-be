package com.shoesbox.domain.social.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
public class KakaoProfile {
    KakaoAccount kakao_account;

    @Data
    public class KakaoAccount {
        private String email;
        private Profile profile;
    }

    @Data
    public class Profile {
        private String nickname;
        private String profile_image_url;
    }
}
