package com.shoesbox.global.exception.runtime.image;

import com.shoesbox.global.exception.ExceptionCode;
import lombok.Getter;

@Getter
public class ImageProcessException extends RuntimeException {
    private final ExceptionCode exceptionCode;
    private final String debugMessage;

    public ImageProcessException(ExceptionCode exceptionCode) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
        this.debugMessage = null;
    }

    public ImageProcessException(ExceptionCode exceptionCode, String message) {
        super(exceptionCode.getMessage());
        this.exceptionCode = exceptionCode;
        this.debugMessage = message;
    }

    public ImageProcessException(ExceptionCode exceptionCode, String message, Throwable cause) {
        super(exceptionCode.getMessage(), cause);
        this.exceptionCode = exceptionCode;
        this.debugMessage = message;
    }
}
