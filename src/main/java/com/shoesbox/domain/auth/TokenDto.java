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
    Long accessTokenLifetimeInMs;
    @NotBlank
    String accessTokenLifetime;
    @NotBlank
    String accessTokenExpireDate;
    @NotNull
    Long refreshTokenLifetimeInMs;
    @NotBlank
    String refreshTokenLifetime;
    @NotBlank
    String refreshTokenExpireDate;
    @NotBlank
    String username;

    @Jacksonized
    @Builder
    public TokenDto(
            String grantType,
            String accessToken,
            String refreshToken,
            Long accessTokenLifetime,
            Long refreshTokenLifetime,
            String username) {
        this.grantType = grantType;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenLifetimeInMs = accessTokenLifetime;
        this.refreshTokenLifetimeInMs = refreshTokenLifetime;
        this.username = username;

        // 토큰 유효시간 MM min, SS sec의 형태로 저장
        this.accessTokenLifetime = String.format("%02d min, %02d sec",
                TimeUnit.MILLISECONDS.toMinutes(accessTokenLifetime),
                TimeUnit.MILLISECONDS.toSeconds(accessTokenLifetime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(accessTokenLifetime))
        );
        this.refreshTokenLifetime = String.format("%02d min, %02d sec",
                TimeUnit.MILLISECONDS.toMinutes(refreshTokenLifetime),
                TimeUnit.MILLISECONDS.toSeconds(refreshTokenLifetime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(refreshTokenLifetime))
        );

        // 토큰 만료시기 yyyy-MM-dd HH:mm:ss 형태로 저장
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SS");
        var now = new Date().getTime();
        this.accessTokenExpireDate =
                dateFormatter.format(now + accessTokenLifetime);
        this.refreshTokenExpireDate =
                dateFormatter.format(now + refreshTokenLifetime);
    }
}
