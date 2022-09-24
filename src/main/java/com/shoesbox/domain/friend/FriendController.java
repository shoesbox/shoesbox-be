package com.shoesbox.domain.friend;

import com.shoesbox.domain.friend.dto.FriendRequestDto;
import com.shoesbox.domain.guest.GuestService;
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
    private final GuestService guestService;

    // 친구 요청
    @PostMapping
    public ResponseEntity<Object> requestFriend(@Valid @RequestBody FriendRequestDto friendRequestDto) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        String currentMemberNickname = SecurityUtil.getCurrentMemberNickname();

        // 게스트계정 테스트중
        guestService.guestCheck(currentMemberId);

        return ResponseHandler.ok(
                friendService.requestFriend(currentMemberId, currentMemberNickname, friendRequestDto));
    }

    // 친구 목록 불러오기
    @GetMapping
    public ResponseEntity<Object> getFriendList() {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.getFriendList(currentMemberId, FriendState.FRIEND));
    }

    // 요청받은 목록
    @GetMapping("/requested")
    public ResponseEntity<Object> getFriendRequestedList() {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.getFriendList(currentMemberId, FriendState.REQUEST));
    }

    // 요청한 목록
    @GetMapping("/request")
    public ResponseEntity<Object> getFriendRequestList() {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(friendService.getFriendRequestList(currentMemberId, FriendState.REQUEST));
    }

    // 요청받은 건 수락
    @PutMapping("/{fromMemberId}/accept")
    public ResponseEntity<Object> acceptFriendRequest(@PathVariable long fromMemberId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 게스트계정 테스트중
        guestService.guestCheck(currentMemberId);

        Friend fromFriend = friendService.findRelationship(fromMemberId, currentMemberId, FriendState.REQUEST);
        return ResponseHandler.ok(friendService.acceptFriendRequest(fromFriend));
    }

    // 요청받은 건 거절
    @DeleteMapping("/{fromMemberId}/refuse")
    public ResponseEntity<Object> refuseFriendRequest(@PathVariable long fromMemberId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 게스트계정 테스트중
        guestService.guestCheck(currentMemberId);

        Friend fromFriend = friendService.findRelationship(fromMemberId, currentMemberId, FriendState.REQUEST);
        return ResponseHandler.ok(friendService.deleteFriendRequest(fromFriend));
    }

    // 요청한 건 취소
    @DeleteMapping("/{toMemberId}/cancel")
    public ResponseEntity<Object> cancelFriendRequest(@PathVariable long toMemberId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 게스트계정 테스트중
        guestService.guestCheck(currentMemberId);

        Friend toFriend = friendService.findRelationship(currentMemberId, toMemberId, FriendState.REQUEST);
        return ResponseHandler.ok(friendService.deleteFriendRequest(toFriend));
    }

    // 친구 삭제
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Object> deleteFriend(@PathVariable long friendId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();

        // 게스트계정 테스트중
        guestService.guestCheck(currentMemberId);

        return ResponseHandler.ok(friendService.deleteFriend(friendId, currentMemberId, FriendState.FRIEND));
    }
}

