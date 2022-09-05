package com.shoesbox.domain.friend.dto;

import com.shoesbox.domain.friend.FriendService;
import com.shoesbox.domain.friend.FriendState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FriendListResponseDto {
    long fromMemberId;
    String fromMemberNickname;
    boolean friendState;

    public FriendListResponseDto(long fromMemberId, String fromMemberNickname, boolean friendState){
        this.fromMemberId = fromMemberId;
        this.fromMemberNickname = fromMemberNickname;
        this.friendState = friendState;
    }
}
