package com.shoesbox.domain.sse.dto;

import com.shoesbox.domain.sse.MessageType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AlarmResponseDto {
    long alarmId;
    long senderMemberId;
    String senderMemberNickname;
    long receiverMemberId;
    MessageType messageType;

    long postId; // post or comment or friend
    int month;
    int day;
}
