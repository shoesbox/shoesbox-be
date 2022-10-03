package com.shoesbox.domain.social;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
@RequiredArgsConstructor
public class OAuthRequestFactory {
    private final KakaoInfo kakaoInfo;
    private final NaverInfo naverInfo;

    public OAuthRequest getRequest(String code, String provider) {
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (provider.equals("kakao")) {
            map.add("grant_type", "authorization_code");
            map.add("client_id", kakaoInfo.getKakaoClientId());
            map.add("redirect_uri", kakaoInfo.getKakaoRedirect());
            map.add("code", code);

            return new OAuthRequest(kakaoInfo.getKakaoTokenUrl(), map);

        } else {
            map.add("grant_type", "authorization_code");
            map.add("client_id", naverInfo.getNaverClientId());
            map.add("client_secret", naverInfo.getNaverClientSecret());
            map.add("redirect_uri", naverInfo.getNaverRedirect());
            map.add("state", "project");
            map.add("code", code);

            return new OAuthRequest(naverInfo.getNaverTokenUrl(), map);
        }
    }

    public String getProfileUrl(String provider) {
        switch (provider) {
            case "kakao":
                return kakaoInfo.getKakaoProfileUrl();
            case "naver":
                return naverInfo.getNaverProfileUrl();
            default:
                return null;
        }
    }

    @Getter
    @Component
    static class KakaoInfo {
        @Value("${spring.social.kakao.client_id}")
        String kakaoClientId;
        @Value("${spring.social.kakao.redirect}")
        String kakaoRedirect;
        @Value("${spring.social.kakao.url.token}")
        private String kakaoTokenUrl;
        @Value("${spring.social.kakao.url.profile}")
        private String kakaoProfileUrl;
    }

    @Getter
    @Component
    static class NaverInfo {
        @Value("${spring.social.naver.client_id}")
        String naverClientId;
        @Value("${spring.social.naver.redirect}")
        String naverRedirect;
        @Value("${spring.social.naver.client_secret}")
        String naverClientSecret;
        @Value("${spring.social.naver.url.token}")
        private String naverTokenUrl;
        @Value("${spring.social.naver.url.profile}")
        private String naverProfileUrl;
    }
}
