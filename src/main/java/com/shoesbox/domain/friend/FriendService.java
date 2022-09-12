package com.shoesbox.domain.friend;

import com.shoesbox.domain.friend.dto.FriendListResponseDto;
import com.shoesbox.domain.friend.dto.FriendRequestDto;
import com.shoesbox.domain.friend.dto.FriendResponseDto;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
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
    public FriendResponseDto requestFriend(long currentUserId, String currentUserName, FriendRequestDto friendRequestDto){

        Member toMember = memberRepository.findByEmail(friendRequestDto.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        Member fromMember = Member.builder()
                .id(currentUserId)
                .nickname(currentUserName)
                .build();

        if(currentUserId == toMember.getId()){
            throw new IllegalArgumentException("자기 자신을 친구추가 할 수 없습니다.");
        }

        // 친구요청 받은 건 중복체크
        Friend requestToMe = friendRepository.findByToMemberIdAndFromMemberId(toMember.getId(), fromMember.getId());
        if(requestToMe != null){
            FriendStateCheck(requestToMe.getFriendState());
        }
        // 요청한 건 중복체크
        Friend requestToFriend = friendRepository.findByToMemberIdAndFromMemberId(fromMember.getId(), toMember.getId());
        if(requestToFriend != null){
            FriendStateCheck(requestToFriend.getFriendState());
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
    public List<FriendListResponseDto> getFriendList(long currentUserId, FriendState friendState) {
        // 친구 요청을 받은 리스트
        List<Friend> fromFriends = friendRepository.findAllByFromMemberIdAndFriendState(currentUserId, friendState);
        // 친구 요청을 한 리스트
        List<Friend> toFriends = friendRepository.findAllByToMemberIdAndFriendState(currentUserId, friendState);

        List<FriendListResponseDto> friendList = new ArrayList<>();
        for (Friend friend : toFriends) {
            friendList.add(toFromFriendListResponseDto(friend));
        }

        // 친구 상태인 경우 요청한 리스트까지 추가
        if (friendState == FriendState.FRIEND){
            for (Friend friend : fromFriends) {
                friendList.add(toToFriendListResponseDto(friend));
            }
        }

        return friendList;
    }

    @Transactional
    public FriendResponseDto acceptFriend(long fromMemberId, long currentUserId, FriendState friendState){
        Friend requestedFriend = friendRepository.findByFromMemberIdAndToMemberIdAndFriendState(fromMemberId, currentUserId, friendState).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        requestedFriend.updateFriendState(FriendState.FRIEND);

        return toFriendResponseDto(requestedFriend);
    }

    public FriendResponseDto refuseFriend(long fromMemberId, long currentUserId, FriendState friendState){
        Friend requestedFriend = friendRepository.findByFromMemberIdAndToMemberIdAndFriendState(fromMemberId, currentUserId, friendState).orElseThrow(
                () -> new IllegalArgumentException("요청 목록에 없는 회원입니다."));

        friendRepository.delete(requestedFriend);
        return toFriendResponseDto(requestedFriend);
    }

    @Transactional
    public FriendListResponseDto deleteFriend(long friendId, long currentUserId, FriendState friendState){
        Friend friend = friendRepository.findByFromMemberIdAndToMemberIdAndFriendState(friendId, currentUserId, friendState).orElse(null);
        FriendListResponseDto responseDto = null;

        // 요청받은 건 삭제 (친구 요청 상태, 친구 상태 모두)
        if(friend != null){
            friendRepository.delete(friend);
            responseDto = toFromFriendListResponseDto(friend);
        }

        // 내가 요청해서 친구가 된 경우 삭제
        if(friendState == FriendState.FRIEND){
            friend = friendRepository.findByFromMemberIdAndToMemberIdAndFriendState(currentUserId, friendId, friendState).orElse(null);
            if(friend != null) {
                friendRepository.delete(friend);
                responseDto = toToFriendListResponseDto(friend);
            }
        }

        // 모두 아닌 경우에는 throw
        if(responseDto == null) {
            throw new IllegalArgumentException("친구 목록에 없는 회원입니다.");
        }

        return responseDto;
    }

    private FriendListResponseDto toFromFriendListResponseDto(Friend friend){
        return FriendListResponseDto.builder()
                .memberId(friend.getFromMember().getId())
                .memberNickname(friend.getFromMember().getNickname())
                .friendState(friend.getFriendState())
                .build();
    }

    private FriendListResponseDto toToFriendListResponseDto(Friend friend){
        return FriendListResponseDto.builder()
                .memberId(friend.getToMember().getId())
                .memberNickname(friend.getToMember().getNickname())
                .friendState(friend.getFriendState())
                .build();
    }

    private FriendResponseDto toFriendResponseDto(Friend friend){
        FriendResponseDto responseDto = FriendResponseDto.builder()
                .toMemberId(friend.getToMember().getId())
                .toMemberNickname(friend.getToMember().getNickname())
                .fromMemberId(friend.getFromMember().getId())
                .fromMemberNickname(friend.getFromMember().getNickname())
                .friendState(friend.getFriendState())
                .build();

        return responseDto;
    }

    private void FriendStateCheck(FriendState friendState){
        if(friendState == FriendState.FRIEND){
            throw new IllegalArgumentException("이미 친구 상태입니다.");
        } else if(friendState == FriendState.REQUEST){
            throw new IllegalArgumentException("이미 친구 요청중입니다.");
        }
    }
}