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
    long sendMemberId;
    String sendMemberNickname;
    long receiveMemberId;
    MessageType messageType;

    long contentId; // post or comment
    int month;
    int day;
}
