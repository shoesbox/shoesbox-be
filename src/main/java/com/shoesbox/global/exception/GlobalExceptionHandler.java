package com.shoesbox.global.exception;

import com.shoesbox.domain.friend.exception.DuplicateFriendRequestException;
import com.shoesbox.domain.member.exception.DuplicateUserInfoException;
import com.shoesbox.domain.post.exception.FileCountLimitExceededException;
import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.exception.apierror.ApiError;
import com.shoesbox.global.exception.runtime.EntityNotFoundException;
import com.shoesbox.global.exception.runtime.InvalidJwtException;
import com.shoesbox.global.exception.runtime.RefreshTokenNotFoundException;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import com.shoesbox.global.exception.runtime.image.ImageProcessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolationException;

import static org.springframework.http.HttpStatus.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * HttpStatus.BAD_REQUEST를 발생시키는 예외 모음
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler(
            {IllegalArgumentException.class,
                    DuplicateUserInfoException.class,
                    BadCredentialsException.class,
                    ConstraintViolationException.class,
                    DuplicateFriendRequestException.class,
                    FileCountLimitExceededException.class})
    protected ResponseEntity<Object> handleBadRequest(RuntimeException ex) {
        return buildResponseEntity(ApiError.builder()
                                           .status(BAD_REQUEST)
                                           .message(ex.getMessage())
                                           .ex(ex)
                                           .build());
    }

    /**
     * 파일 업로드 용량 제한 관련 예외
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler({
            FileSizeLimitExceededException.class,
            SizeLimitExceededException.class})
    protected ResponseEntity<Object> handleBadRequest(SizeException ex) {
        return buildResponseEntity(ApiError.builder()
                                           .status(BAD_REQUEST)
                                           .message("파일 업로드 용량 제한 초과: 파일은 개당 10MB, 최대 50MB까지만 업로드 가능합니다.")
                                           .ex(ex)
                                           .build());
    }

    /**
     * HttpStatus.NOT_FOUND를 발생시키는 예외 모음
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler({RefreshTokenNotFoundException.class, EntityNotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(RuntimeException ex) {
        return buildResponseEntity(ApiError.builder()
                                           .status(NOT_FOUND)
                                           .message(ex.getMessage())
                                           .ex(ex)
                                           .build());
    }

    /**
     * HttpStatus.FORBIDDEN 발생시키는 예외 모음
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler({InvalidJwtException.class, UnAuthorizedException.class})
    protected ResponseEntity<Object> handleJWTVerification(RuntimeException ex) {
        return buildResponseEntity(ApiError.builder()
                                           .status(FORBIDDEN)
                                           .message(ex.getMessage())
                                           .ex(ex)
                                           .build());
    }

    /**
     * 이미지 처리 과정에서 발생하는 예외
     *
     * @param ex the Exception
     * @return the ApiError object
     */
    @ExceptionHandler(ImageProcessException.class)
    protected ResponseEntity<Object> handleImageProcess(ImageProcessException ex) {
        return buildResponseEntity(ApiError.buildImageProcessException()
                                           .status(BAD_REQUEST)
                                           .ex(ex)
                                           .build());
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return ResponseHandler.fail(apiError);
    }
}
