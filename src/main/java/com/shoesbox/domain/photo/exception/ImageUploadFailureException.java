package com.shoesbox.domain.photo.exception;

/**
 * 이미지 업로드 실패 시 발생
 */
public class ImageUploadFailureException extends RuntimeException {
    public ImageUploadFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
