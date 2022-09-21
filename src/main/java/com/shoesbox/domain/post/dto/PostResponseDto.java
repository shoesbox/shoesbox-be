package com.shoesbox.domain.post.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PostResponseDto {
    long postId;
    String title;
    String content;
    long memberId;
    String nickname;
    String createdAt;
    String modifiedAt;
    List<String> images;
}
