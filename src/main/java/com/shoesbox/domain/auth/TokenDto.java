package com.shoesbox.domain.auth;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TokenDto {
    @NotBlank
    String grantType;
    @NotBlank
    String accessToken;
    @NotBlank
    String refreshToken;
    @NotNull
    long accessTokenLifetimeInMs;
    @NotBlank
    String accessTokenLifetime;
    @NotBlank
    long accessTokenExpireDate;
    @NotNull
    long refreshTokenLifetimeInMs;
    @NotBlank
    String refreshTokenLifetime;
    @NotBlank
    long refreshTokenExpireDate;
    @NotBlank
    String email;
    @NotBlank
    String nickname;
    @NotBlank
    long memberId;

    @Jacksonized
    @Builder
    public TokenDto(
            String grantType,
            String accessToken,
            String refreshToken,
            Long accessTokenLifetime,
            Long refreshTokenLifetime,
            String email,
            String nickname,
            long memberId) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenLifetimeInMs = accessTokenLifetime;
        this.refreshTokenLifetimeInMs = refreshTokenLifetime;
        this.email = email;
        this.nickname = nickname;
        this.memberId = memberId;

        // 현재 시간 ms로
        var now = new Date().getTime();
        // 토큰 만료시기 yyyy-MM-dd HH:mm:ss 형태로 저장
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SS");
        this.accessTokenLifetime = dateTimeFormatter.format(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(now + this.accessTokenLifetimeInMs), ZoneId.systemDefault()));
        this.refreshTokenLifetime = dateTimeFormatter.format(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(now + this.refreshTokenLifetimeInMs), ZoneId.systemDefault()));

        this.accessTokenExpireDate = now + this.accessTokenLifetimeInMs;
        this.refreshTokenExpireDate = now + this.refreshTokenLifetimeInMs;
    }
}
