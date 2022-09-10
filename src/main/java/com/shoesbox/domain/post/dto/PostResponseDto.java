package com.shoesbox.domain.post.dto;

import com.shoesbox.domain.comment.CommentResponseDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PostResponseDto {
    Long postId;
    String title;
    String content;
    String nickname;
    long memberId;
    List<CommentResponseDto> comments;
    List<String> imageUrls;
    String createdAt;
    String modifiedAt;
    int createdYear;
    int createdMonth;
    int createdDay;
}
