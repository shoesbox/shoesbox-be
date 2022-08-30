package com.shoesbox.global.exception.runtime;

/**
 * 수정, 삭제 권한 등이 없을 때 발생
 */
public class UnAuthorizedException extends RuntimeException {
    public UnAuthorizedException(String message) {
        super(message);
    }

    public UnAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}