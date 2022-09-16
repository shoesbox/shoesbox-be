package com.shoesbox.domain.comment;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CommentResponseDto {
    long commentId;
    String nickname;
    String content;
    String profileImageUrl;
    long postId;
    long memberId;
    String createdAt;
    String modifiedAt;
}
