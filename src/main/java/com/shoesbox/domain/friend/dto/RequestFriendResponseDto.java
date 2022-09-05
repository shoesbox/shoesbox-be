package com.shoesbox.domain.friend.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RequestFriendResponseDto {
    long toMemberId;
    String toMemberNickname;
    boolean friendState;

    public RequestFriendResponseDto(long toMemberId, String toMemberNickname, boolean friendState){
        this.toMemberId = toMemberId;
        this.toMemberNickname = toMemberNickname;
        this.friendState = friendState;
    }
}
