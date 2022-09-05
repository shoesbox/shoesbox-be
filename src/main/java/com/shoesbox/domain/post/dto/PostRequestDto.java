package com.shoesbox.domain.post.dto;

import lombok.Getter;

@Getter
public class PostRequestDto {
    private String title;
    private String content;
    // private List<MultipartFile> imageFiles;
}
