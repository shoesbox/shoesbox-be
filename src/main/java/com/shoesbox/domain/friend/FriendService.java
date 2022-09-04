package com.shoesbox.domain.friend;

import com.shoesbox.domain.friend.dto.FriendRequestDto;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public String requestFriend(FriendRequestDto friendRequestDto){

        long currentUserId = SecurityUtil.getCurrentMemberIdByLong();
        Member joinedFriend = memberRepository.findByEmail(friendRequestDto.getEmail()).orElseThrow(
                () -> new NullPointerException("해당 회원을 찾을 수 없습니다."));

        if(friendRepository.existsByFriendIdAndAndMemberId(joinedFriend.getId(), currentUserId)){
            throw new RuntimeException("이미 친구 요청한 상태입니다.");
        } else if(currentUserId == joinedFriend.getId()){
            throw new RuntimeException("자기 자신을 친구추가 할 수 없습니다.");
        }

        Member currentUser = Member.builder()
                .id(currentUserId)
                .build();

        Friend friend = Friend.builder()
                .member(currentUser)
                .friendId(joinedFriend.getId())
                .friendName(joinedFriend.getNickname())
                .friendState(FriendState.STATE_REQUEST)
                .build();

        friendRepository.save(friend);

        return "친구 요청 성공!";
    }

}
