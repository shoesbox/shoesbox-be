package com.shoesbox.domain.post.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PostListResponseDto {
    long postId;
    String title;
    String thumbnailUrl;
    String createdAt;
    String modifiedAt;
    int createdYear;
    int createdMonth;
    int createdDay;
}
