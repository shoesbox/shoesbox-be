package com.shoesbox.domain.friend.dto;

import com.shoesbox.domain.friend.FriendState;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendListResponseDto {
    long memberId;
    String memberNickname;
    FriendState friendState;

    public FriendListResponseDto(long memberId, String memberNickname, FriendState friendState) {
        this.memberId = memberId;
        this.memberNickname = memberNickname;
        this.friendState = friendState;
    }
}
