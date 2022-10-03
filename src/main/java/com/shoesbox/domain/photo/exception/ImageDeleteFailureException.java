package com.shoesbox.domain.photo.exception;

/**
 * 이미지 삭제 실패 시 발생
 */
public class ImageDeleteFailureException extends RuntimeException {
    public ImageDeleteFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
