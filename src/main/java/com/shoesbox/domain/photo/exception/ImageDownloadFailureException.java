package com.shoesbox.domain.photo.exception;

/**
 * 이미지 다운로드 실패 시 발생
 */
public class ImageDownloadFailureException extends RuntimeException {
    public ImageDownloadFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
