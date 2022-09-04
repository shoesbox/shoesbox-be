package com.shoesbox.domain.friend.dto;

import com.shoesbox.domain.friend.FriendService;
import com.shoesbox.domain.friend.FriendState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class FriendListResponseDto {
    long requestedFriendId;
    String requestedFriendName;
    FriendState friendState;

    public FriendListResponseDto(long requestedFriendId, String requestedFriendName, FriendState friendState){
        this.requestedFriendId = requestedFriendId;
        this.requestedFriendName = requestedFriendName;
        this.friendState = friendState;
    }
}
