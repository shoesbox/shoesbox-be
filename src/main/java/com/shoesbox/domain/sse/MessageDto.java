package com.shoesbox.domain.sse;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
public class MessageDto {
    String msgType;
    String senderNickName;
    long postId;
    int month;
    int day;
}
