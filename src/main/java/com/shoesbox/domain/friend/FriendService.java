package com.shoesbox.domain.friend;

import com.shoesbox.domain.friend.dto.FriendRequestDto;
import com.shoesbox.domain.friend.dto.FriendResponseDto;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public FriendResponseDto requestFriend(FriendRequestDto friendRequestDto) {

        long currentMemberId = SecurityUtil.getCurrentMemberId();
        String currentMemberNickname = SecurityUtil.getCurrentMemberNickname();
        Member toMember = memberRepository.findByEmail(friendRequestDto.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        if (friendRepository.existsByFromMemberIdAndToMemberId(currentMemberId, toMember.getId())) {
            throw new IllegalArgumentException("이미 친구 요청한 상태입니다.");
        } else if (currentMemberId == toMember.getId()) {
            throw new IllegalArgumentException("자기 자신을 친구추가 할 수 없습니다.");
        }

        Member fromMember = Member.builder()
                .id(currentMemberId)
                .nickname(currentMemberNickname)
                .build();

        if (twoWayCheck(fromMember.getId(), toMember.getId())) {
            throw new IllegalArgumentException("이미 요청 대기중인 친구입니다.");
        }

        Friend friend = Friend.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .friendState(FriendState.REQUEST)
                .build();

        friendRepository.save(friend);

        return toFriendResponseDto(friend);
    }

    @Transactional(readOnly = true)
    public List<FriendResponseDto> getFriendList(FriendState friendState) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        List<Friend> friends = friendRepository.findAllByToMemberIdAndFriendState(currentMemberId, friendState);

        List<FriendResponseDto> friendList = new ArrayList<>();
        for (Friend friend : friends) {
            friendList.add(toFriendResponseDto(friend));
        }
        return friendList;
    }

    @Transactional
    public FriendResponseDto acceptFriend(long fromMemberId, FriendState friendState) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        Friend requestedFriend = friendRepository.findByFromMemberIdAndToMemberIdAndFriendState(
                fromMemberId, currentMemberId, friendState).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        requestedFriend.updateFriendState(FriendState.FRIEND);

        return toFriendResponseDto(requestedFriend);
    }

    @Transactional
    public FriendResponseDto deleteFriend(long fromMemberId, FriendState friendState) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        Friend requestedFriend = friendRepository.findByFromMemberIdAndToMemberIdAndFriendState(
                fromMemberId, currentMemberId, friendState).orElseThrow(
                () -> new IllegalArgumentException("목록에 없는 친구입니다."));

        friendRepository.delete(requestedFriend);

        return toFriendResponseDto(requestedFriend);
    }

    private FriendResponseDto toFriendResponseDto(Friend friend) {
        return FriendResponseDto.builder()
                .toMemberId(friend.getToMember().getId())
                .toMemberNickname(friend.getToMember().getNickname())
                .fromMemberId(friend.getFromMember().getId())
                .fromMemberNickname(friend.getFromMember().getNickname())
                .friendState(friend.getFriendState())
                .build();
    }

    private boolean twoWayCheck(long fromMemberId, long toMemberId) {
        // 요청값과 반대의 경우가 존재하는지(쌍방 요청인지) 체크
        // 상대방이 요청한 이력이 있을 경우 친구 요청 불가(요청리스트에서 수락)
        return friendRepository.existsByFromMemberIdAndToMemberId(toMemberId, fromMemberId);
    }
}
