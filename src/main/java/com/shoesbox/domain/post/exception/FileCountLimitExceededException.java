package com.shoesbox.domain.post.exception;

/**
 * 첨부 파일 개수가 제한을 초과할 시 발생
 */
public class FileCountLimitExceededException extends RuntimeException {
    public FileCountLimitExceededException(String message) {
        super(message);
    }

    public FileCountLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
