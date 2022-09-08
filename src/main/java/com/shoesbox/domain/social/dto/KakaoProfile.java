package com.shoesbox.domain.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
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
