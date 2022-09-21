package com.shoesbox.domain.friend.exception;

/**
 * 친구 요청이 중복된 경우 발생
 */
public class DuplicateFriendRequestException extends RuntimeException {
    public DuplicateFriendRequestException(String message) {
        super(message);
    }

    public DuplicateFriendRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
