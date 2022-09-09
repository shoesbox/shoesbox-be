package com.shoesbox.domain.social;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.shoesbox.domain.auth.RefreshToken;
import com.shoesbox.domain.auth.RefreshTokenRepository;
import com.shoesbox.domain.auth.TokenDto;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.domain.social.dto.GoogleProfile;
import com.shoesbox.domain.social.dto.KakaoProfile;
import com.shoesbox.domain.social.dto.NaverProfile;
import com.shoesbox.domain.social.dto.ProfileDto;
import com.shoesbox.global.security.CustomUserDetails;
import com.shoesbox.global.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProviderService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final OAuthRequestFactory oAuthRequestFactory;
    private final TokenProvider tokenProvider;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Gson gson;

    @Transactional
    public TokenDto SocialLogin(String code, String provider) throws JsonProcessingException {
        String accessToken = getAccessToken(code, provider);

        ProfileDto profileDto = getProfile(accessToken, provider);

        Member member = memberRepository.findByEmail(profileDto.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저 정보가 없습니다"));
        if (member != null) {
            // db에 있을 시 토큰 생성
            return getTokenInfo(member);
        } else{
            // db에 없을 경우 등록 후 토큰 생성
            String password = UUID.randomUUID().toString(); // 랜덤 password 생성
            member = Member.builder()
                    .email(profileDto.getEmail())
                    .password(bCryptPasswordEncoder.encode(password))
                    .nickname(profileDto.getNickname())
                    .profileImageUrl(profileDto.getProfileImage())
                    .selfDescription(provider + " 유저입니다.")
                    .build();
            memberRepository.save(member);

            return getTokenInfo(member);
        }
    }

    public TokenDto getTokenInfo(Member member){

        // 강제 로그인 처리
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(member.getAuthority().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        CustomUserDetails principal = CustomUserDetails.builder()
                .email(member.getEmail())
                .memberId(member.getId())
                .authorities(authorities)
                .build();

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, authorities));

        // 토큰 생성
        TokenDto tokenDto = tokenProvider.createTokenDto(principal);

        // db에 refreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .memberId(member.getId())
                .tokenValue(tokenDto.getRefreshToken())
                .build();
        refreshTokenRepository.save(refreshToken);

        // 6. 토큰 발급
        return tokenDto;
    }

    private String getAccessToken(String code, String provider) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // provider로 선택한 소셜 정보를 불러와 oAuthRequest에 저장
        OAuthRequest oAuthRequest = oAuthRequestFactory.getRequest(code, provider);
        HttpEntity<LinkedMultiValueMap<String, String>> request = new HttpEntity<>(oAuthRequest.getMap(), headers);

        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.postForEntity(oAuthRequest.getTokenUrl(), request, String.class);

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        return jsonNode.get("access_token").asText();
    }

    public ProfileDto getProfile(String accessToken, String provider) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        String profileUrl = oAuthRequestFactory.getProfileUrl(provider);
        HttpEntity<LinkedMultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.postForEntity(profileUrl, request, String.class);

        return extractProfile(response, provider);
    }

    private ProfileDto extractProfile(ResponseEntity<String> response, String provider) throws JsonProcessingException {
        if (provider.equals("kakao")) {
            KakaoProfile kakaoProfile = gson.fromJson(response.getBody(), KakaoProfile.class);
            return ProfileDto.builder()
                    .email(kakaoProfile.getKakao_account().getEmail())
                    .nickname(kakaoProfile.getKakao_account().getProfile().getNickname())
                    .profileImage(kakaoProfile.getKakao_account().getProfile().getProfile_image_url())
                    .build();
        } else if(provider.equals("naver")) {
            NaverProfile naverProfile = gson.fromJson(response.getBody(), NaverProfile.class);
            return ProfileDto.builder()
                    .email(naverProfile.getResponse().getEmail())
                    .nickname(naverProfile.getResponse().getNickname())
                    .profileImage(naverProfile.getResponse().getProfile_image())
                    .build();
        } else{
            GoogleProfile googleProfile = gson.fromJson(response.getBody(), GoogleProfile.class);
            return ProfileDto.builder()
                    .email(googleProfile.getEmail())
                    .nickname(googleProfile.getName())
                    .profileImage(googleProfile.getPicture())
                    .build();
        }
    }
}
