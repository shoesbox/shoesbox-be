package com.shoesbox.global.security.jwt;

import lombok.Getter;

@Getter
public enum ExceptionCode {
    INVALID_SIGNATURE_TOKEN("1001",
            "Invalid JWT signature, 유효하지 않은 JWT 서명 입니다."),
    EXPIRED_TOKEN("1002",
            "Expired JWT token, 만료된 JWT token 입니다. 토큰을 재발급 받아야 합니다."),
    UNSUPPORTED_TOKEN("1003",
            "Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다."),
    WRONG_TOKEN("1004",
            "JWT claims is empty, 잘못된 JWT 토큰 입니다."),
    LOGGED_OUT_TOKEN("1005",
            "로그아웃한 유저입니다."),
    INVALID_AUTHORITIES_TOKEN("1006",
            "권한 정보가 없는 토큰입니다."),
    UNKNOWN_ERROR("1007", "Unknown error"),
    ACCESS_DENIED("1008", "접근 거부.");

    private final String code;
    private final String message;

    ExceptionCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
