package com.shoesbox.domain.friend.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FriendResponseDto {
    private long toMemberId;
    private String toMemberNickname;
    private long fromMemberId;
    private String fromMemberNickname;
    private boolean friendState;

    @Builder
    public FriendResponseDto(long toMemberId, String toMemberNickname, long fromMemberId, String fromMemberNickname, boolean friendState){
        this.toMemberId = toMemberId;
        this.toMemberNickname = toMemberNickname;
        this.fromMemberId = fromMemberId;
        this.fromMemberNickname = fromMemberNickname;
        this.friendState = friendState;
    }
}
