package com.shoesbox.domain.post.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PostRequestDto {
    private String title;
    private String content;
    private String images;
}
