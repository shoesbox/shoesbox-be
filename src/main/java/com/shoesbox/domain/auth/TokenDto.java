package com.shoesbox.domain.auth;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    String username;

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
            String username,
            long memberId) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenLifetimeInMs = accessTokenLifetime;
        this.refreshTokenLifetimeInMs = refreshTokenLifetime;
        this.username = username;
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

        // 토큰 유효시간 MM min, SS sec의 형태로 저장
        // this.accessTokenLifetime = String.format("%02d min, %02d sec",
        //         TimeUnit.MILLISECONDS.toMinutes(accessTokenLifetime),
        //         TimeUnit.MILLISECONDS.toSeconds(accessTokenLifetime) -
        //                 TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(accessTokenLifetime))
        // );
        // this.refreshTokenLifetime = String.format("%02d min, %02d sec",
        //         TimeUnit.MILLISECONDS.toMinutes(refreshTokenLifetime),
        //         TimeUnit.MILLISECONDS.toSeconds(refreshTokenLifetime) -
        //                 TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(refreshTokenLifetime))
        // );
    }
}
