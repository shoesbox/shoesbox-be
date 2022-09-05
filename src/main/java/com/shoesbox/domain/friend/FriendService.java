package com.shoesbox.domain.friend;

import com.shoesbox.domain.friend.dto.FriendListResponseDto;
import com.shoesbox.domain.friend.dto.FriendRequestDto;
import com.shoesbox.domain.friend.dto.RequestFriendResponseDto;
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
    public RequestFriendResponseDto requestFriend(FriendRequestDto friendRequestDto){

        long currentUserId = SecurityUtil.getCurrentMemberIdByLong();
        Member toMember = memberRepository.findByEmail(friendRequestDto.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        if(friendRepository.existsByFromMemberIdAndToMemberId(currentUserId, toMember.getId())){
            throw new IllegalArgumentException("이미 친구 요청한 상태입니다.");
        } else if(currentUserId == toMember.getId()){
            throw new IllegalArgumentException("자기 자신을 친구추가 할 수 없습니다.");
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

        return new RequestFriendResponseDto(friend.getToMember().getId(), friend.getToMember().getNickname(), friend.isFriendState());
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
    public FriendListResponseDto acceptFriend(long fromMemberId, boolean friendState){
        Friend requestedFriend = friendRepository.findByFromMemberIdAndFriendState(fromMemberId, friendState).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        requestedFriend.updateFriendState(true);

        return toFriendListResponseDto(requestedFriend);
    }

    @Transactional
    public FriendListResponseDto deleteFriend(long fromMemberId, boolean friendState){
        Friend requestedFriend = friendRepository.findByFromMemberIdAndFriendState(fromMemberId, friendState).orElseThrow(
                () -> new IllegalArgumentException("목록에 없는 친구입니다."));

        friendRepository.delete(requestedFriend);

        return toFriendListResponseDto(requestedFriend);
    }

    private FriendListResponseDto toFriendListResponseDto(Friend friend){
        return new FriendListResponseDto(friend.getFromMember().getId(), friend.getFromMember().getNickname(), friend.isFriendState());
    }
}
