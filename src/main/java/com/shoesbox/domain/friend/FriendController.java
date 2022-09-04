package com.shoesbox.domain.friend;

import com.shoesbox.domain.friend.dto.FriendListResponseDto;
import com.shoesbox.domain.friend.dto.FriendRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/api/friends")
@RequiredArgsConstructor
@RestController
public class FriendController {

    private final FriendService friendService;

    @PostMapping
    public String requestFriend(@Valid @RequestBody FriendRequestDto friendRequestDto){
        return friendService.requestFriend(friendRequestDto);
    }

    @GetMapping
    public List<FriendListResponseDto> getFriendList(){
        return friendService.getFriendList(FriendState.STATE_ACCEPT);
    }

    @GetMapping("/request")
    public List<FriendListResponseDto> getFriendRequestList(){
        return friendService.getFriendList(FriendState.STATE_REQUEST);
    }
}
