package com.shoesbox.global.common;

import com.shoesbox.global.exception.apierror.ApiError;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ResponseWrapper<T> {
    private boolean success;
    private T data;
    private ApiError errorDetails;

    public static <T> ResponseEntity<ResponseWrapper<T>> ok(T data) {
        return ResponseEntity.ok(ResponseWrapper.<T>builder()
                .success(true)
                .data(data)
                .errorDetails(null)
                .build());
    }

    public static <T> ResponseWrapper<T> fail(ApiError apiError) {
        return ResponseWrapper.<T>builder()
                .success(false)
                .data(null)
                .errorDetails(apiError)
                .build();
    }
}
