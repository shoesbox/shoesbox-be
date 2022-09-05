package com.shoesbox.domain.friend;

import com.shoesbox.domain.friend.dto.FriendRequestDto;
import com.shoesbox.global.common.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/api/friends")
@RequiredArgsConstructor
@RestController
public class FriendController {

    private final FriendService friendService;

    @PostMapping
    public ResponseEntity<Object> requestFriend(@Valid @RequestBody FriendRequestDto friendRequestDto){
        return ResponseHandler.ok(friendService.requestFriend(friendRequestDto));
    }

    @GetMapping
    public ResponseEntity<Object> getFriendList(){
        return ResponseHandler.ok(friendService.getFriendList(true));
    }

    @GetMapping("/request")
    public ResponseEntity<Object> getFriendRequestList(){
        return ResponseHandler.ok(friendService.getFriendList(false));
    }

    @PutMapping("/{fromMemberId}/accept")
    public ResponseEntity<Object> acceptFriend(@PathVariable long fromMemberId){
        return ResponseHandler.ok(friendService.acceptFriend(fromMemberId, false));
    }

    @DeleteMapping("/{fromMemberId}/refuse")
    public ResponseEntity<Object> refuseFriend(@PathVariable long fromMemberId){
        return ResponseHandler.ok(friendService.deleteFriend(fromMemberId, false));
    }

    @DeleteMapping("/{fromMemberId}")
    public ResponseEntity<Object> deleteFriend(@PathVariable long fromMemberId){
        return ResponseHandler.ok(friendService.deleteFriend(fromMemberId, true));
    }
}