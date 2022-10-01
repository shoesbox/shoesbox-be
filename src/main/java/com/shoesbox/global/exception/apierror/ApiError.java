package com.shoesbox.global.exception.apierror;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.shoesbox.global.util.LowerCaseClassNameResolver;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.validation.ConstraintViolation;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.CUSTOM, property = "error",
        visible = true)
@JsonTypeIdResolver(LowerCaseClassNameResolver.class)
public class ApiError {

    private HttpStatus status;
    private int httpStatusCode;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss:SS")
    private LocalDateTime timestamp;
    private String originalExceptionType;
    private String customExceptionType;
    private String message;
    private String debugMessage;
    private List<ApiSubError> subErrors;

    private ApiError() {
        timestamp = LocalDateTime.now();
    }

    @Builder
    protected ApiError(HttpStatus status, String message, Throwable ex) {
        this();
        this.status = status;
        this.httpStatusCode = status.value();
        this.originalExceptionType = (ex.getCause() != null) ? ex.getCause().getClass().getTypeName() : null;
        this.customExceptionType = ex.getClass().getTypeName();
        this.message = message;
        if (!message.equals(ex.getLocalizedMessage())) {
            this.debugMessage = ex.getLocalizedMessage();
        }
    }

    private void addSubError(ApiSubError subError) {
        if (subErrors == null) {
            subErrors = new ArrayList<>();
        }
        subErrors.add(subError);
    }

    private void addValidationError(String object, String field, Object rejectedValue, String message) {
        addSubError(new ApiValidationError(object, field, rejectedValue, message));
    }

    private void addValidationError(String object, String message) {
        addSubError(new ApiValidationError(object, message));
    }

    private void addValidationError(FieldError fieldError) {
        this.addValidationError(
                fieldError.getObjectName(),
                fieldError.getField(),
                fieldError.getRejectedValue(),
                fieldError.getDefaultMessage());
    }

    public void addValidationErrors(List<FieldError> fieldErrors) {
        fieldErrors.forEach(this::addValidationError);
    }

    private void addValidationError(ObjectError objectError) {
        this.addValidationError(
                objectError.getObjectName(),
                objectError.getDefaultMessage());
    }

    public void addValidationError(List<ObjectError> globalErrors) {
        globalErrors.forEach(this::addValidationError);
    }

    /**
     * Utility method for adding error of ConstraintViolation. Usually when a @Validated validation fails.
     *
     * @param cv the ConstraintViolation
     */
    private void addValidationError(ConstraintViolation<?> cv) {
        this.addValidationError(
                cv.getRootBeanClass().getSimpleName(),
                ((PathImpl) cv.getPropertyPath()).getLeafNode().asString(),
                cv.getInvalidValue(),
                cv.getMessage());
    }

    public void addValidationErrors(Set<ConstraintViolation<?>> constraintViolations) {
        constraintViolations.forEach(this::addValidationError);
    }

}
