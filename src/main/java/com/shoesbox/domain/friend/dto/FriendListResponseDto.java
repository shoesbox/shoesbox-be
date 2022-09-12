package com.shoesbox.domain.friend.dto;

import com.shoesbox.domain.friend.FriendState;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FriendListResponseDto {
    long memberId;
    String memberNickname;
    FriendState friendState;

    @Builder
    public FriendListResponseDto (long memberId, String memberNickname, FriendState friendState){
        this.memberId = memberId;
        this.memberNickname = memberNickname;
        this.friendState = friendState;
    }
}
