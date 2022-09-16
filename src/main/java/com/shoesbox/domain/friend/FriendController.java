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

    // 친구 요청
    @PostMapping
    public ResponseEntity<Object> requestFriend(@Valid @RequestBody FriendRequestDto friendRequestDto) {
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        String currentMemberNickname = SecurityUtil.getCurrentMemberNickname();
        return ResponseHandler.ok(
                friendService.requestFriend(currnetMemberId, currentMemberNickname, friendRequestDto));
    }

    // 친구 목록 불러오기
    @GetMapping
    public ResponseEntity<Object> getFriendList() {
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.getFriendList(currnetMemberId, FriendState.FRIEND));
    }

    // 요청받은 목록
    @GetMapping("/requested")
    public ResponseEntity<Object> getFriendRequestedList() {
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.getFriendList(currnetMemberId, FriendState.REQUEST));
    }

    // 요청한 목록
    @GetMapping("/request")
    public ResponseEntity<Object> getFriendRequestList() {
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.getFriendRequestList(currnetMemberId, FriendState.REQUEST));
    }

    // 요청받은 건 수락
    @PutMapping("/{fromMemberId}/accept")
    public ResponseEntity<Object> acceptFriendRequest(@PathVariable long fromMemberId) {
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        Friend fromFriend = friendService.findRelationship(fromMemberId, currnetMemberId, FriendState.REQUEST);
        return ResponseHandler.ok(friendService.acceptFriendRequest(fromFriend));
    }

    // 요청받은 건 거절
    @DeleteMapping("/{fromMemberId}/refuse")
    public ResponseEntity<Object> refuseFriendRequest(@PathVariable long fromMemberId) {
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        Friend fromFriend = friendService.findRelationship(fromMemberId, currnetMemberId, FriendState.REQUEST);
        return ResponseHandler.ok(friendService.deleteFriendRequest(fromFriend));
    }

    // 요청한 건 취소
    @DeleteMapping("/{toMemberId}/cancle")
    public ResponseEntity<Object> cancleFriendRequest(@PathVariable long toMemberId) {
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        Friend toFriend = friendService.findRelationship(currnetMemberId, toMemberId, FriendState.REQUEST);
        return ResponseHandler.ok(friendService.deleteFriendRequest(toFriend));
    }

    // 친구 삭제
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Object> deleteFriend(@PathVariable long friendId) {
        long currnetMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.deleteFriend(friendId, currnetMemberId, FriendState.FRIEND));
    }
}

