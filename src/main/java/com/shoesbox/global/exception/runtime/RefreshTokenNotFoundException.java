package com.shoesbox.global.exception.runtime;

/**
 * db에서 리프레쉬 토큰을 찾을 수 없을 때 발생
 */
public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException(String message) {
        this(message, null);
    }

    public RefreshTokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
