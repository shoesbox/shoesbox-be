package com.shoesbox.domain.friend;

import com.shoesbox.domain.friend.dto.FriendListResponseDto;
import com.shoesbox.domain.friend.dto.FriendRequestDto;
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
    public String requestFriend(FriendRequestDto friendRequestDto){

        long currentUserId = SecurityUtil.getCurrentMemberIdByLong();
        Member toMember = memberRepository.findByEmail(friendRequestDto.getEmail()).orElseThrow(
                () -> new NullPointerException("해당 회원을 찾을 수 없습니다."));

        if(friendRepository.existsByFromMemberIdAndToMemberId(currentUserId, toMember.getId())){
            throw new RuntimeException("이미 친구 요청한 상태입니다.");
        } else if(currentUserId == toMember.getId()){
            throw new RuntimeException("자기 자신을 친구추가 할 수 없습니다.");
        }

        Member fromMember = Member.builder()
                .id(currentUserId)
                .build();

        Friend friend = Friend.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .friendState(false)
                .build();

        friendRepository.save(friend);

        return "친구 요청 성공!";
    }

    @Transactional(readOnly = true)
    public List<FriendListResponseDto> getFriendList(boolean friendState) {
        long currentUserId = SecurityUtil.getCurrentMemberIdByLong();
        List<Friend> friends = friendRepository.findAllByToMemberIdAndFriendState(currentUserId, friendState);

        List<FriendListResponseDto> friendList = new ArrayList<>();
        for (Friend friend : friends) {
            friendList.add(new FriendListResponseDto(friend.getFromMemberId(), friend.getFromMember().getNickname(), friend.isFriendState()));
        }
        return friendList;
    }

    @Transactional
    public String acceptFriend(long fromMemberId, boolean friendState){
        Friend requestedFriend = friendRepository.findByFromMemberIdAndFriendState(fromMemberId, friendState).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        requestedFriend.updateFriendState(true);

        return "친구 수락 완료!";
    }

    @Transactional
    public String deleteFriend(long fromMemberId, boolean friendState){
        Friend requestedFriend = friendRepository.findByFromMemberIdAndFriendState(fromMemberId, friendState).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        friendRepository.delete(requestedFriend);

        return friendState?"친구 삭제 완료":"친구 요청 거부 완료";
    }
}
