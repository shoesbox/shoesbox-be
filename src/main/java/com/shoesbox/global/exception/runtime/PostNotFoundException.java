package com.shoesbox.global.exception.runtime;

/**
 * DB에서 Post를 찾을 수 없을 때 발생
 */
public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(String message) {
        super(message);
    }

    public PostNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}