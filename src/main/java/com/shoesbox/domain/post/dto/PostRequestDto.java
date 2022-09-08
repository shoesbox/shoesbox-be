package com.shoesbox.domain.post.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Jacksonized
@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PostRequestDto {
    private String title;
    private String content;
    private List<MultipartFile> imageFiles;
}