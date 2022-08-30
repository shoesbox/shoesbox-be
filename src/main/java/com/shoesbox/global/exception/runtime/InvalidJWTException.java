package com.shoesbox.global.exception.runtime;

/**
 * JWT 토큰 유효성 검증에 실패했을 때 발생
 */
public class InvalidJWTException extends RuntimeException {
    public InvalidJWTException(String message) {
        this(message, null);
    }

    public InvalidJWTException(String message, Throwable cause) {
        super(message, cause);
    }
}
