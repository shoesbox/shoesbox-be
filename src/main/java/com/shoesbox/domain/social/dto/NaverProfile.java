package com.shoesbox.domain.social.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NaverProfile {
    Response response;

    @Data
    public class Response {
        private String id;
        private String email;
        private String nickname;
        private String profile_image;
    }
}
