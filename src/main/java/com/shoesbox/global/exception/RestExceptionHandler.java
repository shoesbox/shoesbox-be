package com.shoesbox.global.exception;

import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.exception.apierror.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;

import static org.springframework.http.HttpStatus.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 컨트롤러 메서드에 @RequestParam 어노테이션이 있는데 url에는 해당 파라미터가 없을 때 발생
     *
     * @param ex      MissingServletRequestParameterException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        String error =
                ex.getMessage() + ", " + ex.getSupportedMediaTypes().stream()
                        .map(String::valueOf) + ", " + Arrays.toString(ex.getStackTrace());
        var apiError = ApiError.builder()
                .status(BAD_REQUEST)
                .message(error)
                .ex(ex)
                .build();
        return ResponseHandler.fail(apiError);
    }

    /**
     * 컨트롤러 메서드에 @RequestParam 어노테이션이 있는데 url에는 해당 파라미터가 없을 때 발생
     *
     * @param ex      MissingServletRequestParameterException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        String error = String.format(
                "Missing request parameter '%s'",
                ex.getParameterName());
        var apiError = ApiError.builder()
                .status(BAD_REQUEST)
                .message(error)
                .ex(ex)
                .build();
        return ResponseHandler.fail(apiError);
    }

    /**
     * 요청의 ContentType이 x-www-form-urlencoded일 때, 컨트롤러 메서드의 매개변수가 Class 형태면 발생
     *
     * @param ex      HttpMediaTypeNotSupportedException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        var builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" 타입은 지원하지 않습니다. ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));
        builder.append("을 사용해 주십시오.  ");
        var apiError = ApiError.builder()
                .status(UNSUPPORTED_MEDIA_TYPE)
                .message(builder.substring(0, builder.length() - 2))
                .ex(ex)
                .build();
        return ResponseHandler.fail(apiError);
    }

    /**
     * 컨트롤러에서 입력받는 값이 @Valid 검증을 통과하지 못했을 때 발생
     *
     * @param ex      the MethodArgumentNotValidException that is thrown when @Valid validation fails
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        var apiError = ApiError.builder()
                .status(BAD_REQUEST)
                .message("Validation error. 값이 올바르지 않습니다.")
                .ex(ex)
                .build();
        apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
        apiError.addValidationError(ex.getBindingResult().getGlobalErrors());
        return ResponseHandler.fail(apiError);
    }

    /**
     * 요청으로 받은 JSON의 형식이 잘못 되었을 때(ex. key가 누락된 json) 발생
     *
     * @param ex      HttpMessageNotReadableException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        var servletWebRequest = (ServletWebRequest) request;
        log.info("{} to {}", servletWebRequest.getHttpMethod(), servletWebRequest.getRequest().getServletPath());
        var error = "Malformed JSON request." +
                "정상적인 JSON 요청이 아닙니다. 형식이 올바른지 확인하십시오.";
        var apiError = ApiError.builder()
                .status(BAD_REQUEST)
                .message(error)
                .ex(ex)
                .build();
        return ResponseHandler.fail(apiError);
    }

    /**
     * Spring이 반환된 객체의 프로퍼티를 가져올 수 없을 때 발생
     *
     * @param ex      HttpMessageNotWritableException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex,
            HttpHeaders headers, HttpStatus status,
            WebRequest request) {
        var error = "Error writing JSON output. " +
                "기본 생성자, Getters, Jackson 의존성이 있는지 확인하십시오.";
        var apiError = ApiError.builder()
                .status(INTERNAL_SERVER_ERROR)
                .message(error)
                .ex(ex)
                .build();
        return ResponseHandler.fail(apiError);
    }

    /**
     * 존재하지 않는 주소로 HTTP 요청을 보낼 때 발생
     *
     * @param ex      NoHandlerFoundException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        var error = String.format(
                "URL %s 에 대한 %s Method를 찾을 수 없습니다.",
                ex.getRequestURL(),
                ex.getHttpMethod());
        var apiError = ApiError.builder()
                .status(BAD_REQUEST)
                .message(error)
                .ex(ex)
                .build();
        return ResponseHandler.fail(apiError);
    }
}
