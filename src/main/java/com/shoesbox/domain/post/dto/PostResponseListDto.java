package com.shoesbox.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PostResponseListDto {
    long postId;
    String thumbnailUrl;
    @JsonIgnore
    int createdMonth;
    int createdDay;
}
