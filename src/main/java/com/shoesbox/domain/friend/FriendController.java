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
        long currentUserId = SecurityUtil.getCurrentMemberId();
        String currentUserName = SecurityUtil.getCurrentMemberNickname();
        return ResponseHandler.ok(friendService.requestFriend(currentUserId, currentUserName, friendRequestDto));
    }

    @GetMapping
    public ResponseEntity<Object> getFriendList(){
        long currentUserId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.getFriendList(currentUserId, FriendState.FRIEND));
    }

    @GetMapping("/request")
    public ResponseEntity<Object> getFriendRequestList(){
        long currentUserId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.getFriendList(currentUserId, FriendState.REQUEST));
    }

    @PutMapping("/{fromMemberId}/accept")
    public ResponseEntity<Object> acceptFriend(@PathVariable long fromMemberId){
        long currentUserId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.acceptFriend(fromMemberId, currentUserId, FriendState.REQUEST));
    }

    @DeleteMapping("/{fromMemberId}/refuse")
    public ResponseEntity<Object> refuseFriend(@PathVariable long fromMemberId){
        long currentUserId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.refuseFriend(fromMemberId, currentUserId, FriendState.REQUEST));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Object> deleteFriend(@PathVariable long friendId){
        long currentUserId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.deleteFriend(friendId, currentUserId, FriendState.FRIEND));
    }
}