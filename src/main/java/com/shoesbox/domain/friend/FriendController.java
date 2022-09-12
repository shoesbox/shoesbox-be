package com.shoesbox.domain.friend;

import com.shoesbox.domain.friend.dto.FriendRequestDto;
import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/api/friends")
@RequiredArgsConstructor
@RestController
public class FriendController {

    private final FriendService friendService;

    @PostMapping
    public ResponseEntity<Object> requestFriend(@Valid @RequestBody FriendRequestDto friendRequestDto){
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        String currentMemberNickname = SecurityUtil.getCurrentMemberNickname();
        return ResponseHandler.ok(friendService.requestFriend(currnetMemberId, currentMemberNickname, friendRequestDto));
    }

    @GetMapping
    public ResponseEntity<Object> getFriendList(){
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.getFriendList(currnetMemberId, FriendState.FRIEND));
    }

    @GetMapping("/request")
    public ResponseEntity<Object> getFriendRequestList(){
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.getFriendList(currnetMemberId, FriendState.REQUEST));
    }

    @PutMapping("/{fromMemberId}/accept")
    public ResponseEntity<Object> acceptFriend(@PathVariable long fromMemberId){
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.acceptFriend(fromMemberId, currnetMemberId, FriendState.REQUEST));
    }

    @DeleteMapping("/{fromMemberId}/refuse")
    public ResponseEntity<Object> refuseFriend(@PathVariable long fromMemberId){
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.refuseFriend(fromMemberId, currnetMemberId, FriendState.REQUEST));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Object> deleteFriend(@PathVariable long friendId){
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.deleteFriend(friendId, currnetMemberId, FriendState.FRIEND));
    }
}
