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
        return friendService.getFriendList(true);
    }

    @GetMapping("/request")
    public List<FriendListResponseDto> getFriendRequestList(){
        return friendService.getFriendList(false);
    }

    @PutMapping("/{fromMemberId}/accept")
    public String acceptFriend(@PathVariable long fromMemberId){
        return friendService.acceptFriend(fromMemberId, false);
    }

    @DeleteMapping("/{fromMemberId}/refuse")
    public String refuseFriend(@PathVariable long fromMemberId){
        return friendService.deleteFriend(fromMemberId, false);
    }

    @DeleteMapping("/{fromMemberId}")
    public String deleteFriend(@PathVariable long fromMemberId){
        return friendService.deleteFriend(fromMemberId, true);
    }
}