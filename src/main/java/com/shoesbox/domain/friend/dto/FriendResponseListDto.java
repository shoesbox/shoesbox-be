package com.shoesbox.domain.friend.dto;

import com.shoesbox.domain.friend.FriendState;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendResponseListDto {
    long friendId;
    long memberId;
    String memberNickname;
    FriendState friendState;
}
