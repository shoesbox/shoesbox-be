package com.shoesbox.global.exception.runtime;

/**
 * JWT 토큰 유효성 검증에 실패했을 때 발생
 */
public class InvalidJwtException extends RuntimeException {
    public InvalidJwtException(String message) {
        this(message, null);
    }

    public InvalidJwtException(String message, Throwable cause) {
        super(message, cause);
    }
}
