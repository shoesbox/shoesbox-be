package com.shoesbox.domain.photo.exception;

/**
 * 이미지 변환 실패 시 발생
 */
public class ImageConvertFailureException extends RuntimeException {
    public ImageConvertFailureException(String message) {
        super(message);
    }

    public ImageConvertFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
