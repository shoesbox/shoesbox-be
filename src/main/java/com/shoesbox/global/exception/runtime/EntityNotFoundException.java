package com.shoesbox.global.exception.runtime;

/**
 * DB에서 Entity를 찾을 수 없을 때 발생
 */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityType) {
        super(entityType + "를/을 찾을 수 없습니다.");
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
