package com.shoesbox.global.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExceptionCode {
    IMAGE_CONVERT_FAILURE("IMAGE_CONVERT_FAILURE", "이미지 변환에 실패했습니다."),
    IMAGE_DELETE_FAILURE("IMAGE_DELETE_FAILURE", "이미지 삭제에 실패했습니다."),
    IMAGE_DOWNLOAD_FAILURE("IMAGE_DOWNLOAD_FAILURE", "이미지 다운로드에 실패했습니다."),
    IMAGE_UPLOAD_FAILURE("IMAGE_UPLOAD_FAILURE", "이미지 업로드에 실패했습니다."),
    IMAGE_RESIZE_FAILURE("IMAGE_RESIZE_FAILURE", "이미지 리사이징에 실패했습니다."),
    IMAGE_ROTATE_FAILURE("IMAGE_ROTATE_FAILURE", "이미지 회전에 실패했습니다."),
    INVALID_IMAGE_FORMAT("INVALID_IMAGE_FORMAT", "이미지 파일 형식은 bmp, jpg, jpeg, png, webp 중 하나여야 합니다.");


    private final String code;
    private final String message;
}
