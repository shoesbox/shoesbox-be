package com.shoesbox.domain.guest;

import com.shoesbox.domain.friend.Friend;
import com.shoesbox.domain.friend.FriendRepository;
import com.shoesbox.domain.friend.FriendState;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.global.exception.runtime.EntityNotFoundException;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class GuestService {

    @Value("guest")
    private String guestId;
    @Value("@test.com")
    private String testEmail;

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;


    @Transactional(readOnly = true)
    public boolean isGuest(long currentMemberId) {
        Member currentMember = memberRepository.findById(currentMemberId).orElseThrow(() -> new EntityNotFoundException(
                Member.class.getPackageName()));

        return currentMember.getEmail().contains(guestId) && currentMember.getEmail().contains(testEmail);
    }

    @Transactional(readOnly = true)
    public boolean isJoinedGuest(String guestEmail) {
        return memberRepository.existsByEmail(guestEmail);
    }

    @Transactional
    public String makeFriendToGuest(String fromMemberEmail, String toMemberEmail, FriendState friendState) {
        Member fromMember = memberRepository.findByEmail(fromMemberEmail).orElseThrow(() -> new UsernameNotFoundException("친구 계정을 찾을 수 없습니다"));
        Member toMember = memberRepository.findByEmail(toMemberEmail).orElseThrow(() -> new UsernameNotFoundException("게스트 계정을 찾을 수 없습니다"));

        Friend friend = Friend.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .friendState(friendState)
                .build();

        friendRepository.save(friend);

        return "From : " + friend.getFromMember().getNickname() + " / To : " + friend.getToMember().getNickname();
    }

    public void guestCheck(long currentMemberId) {
        if (isGuest(currentMemberId)) throw new UnAuthorizedException("체험용 계정입니다. 회원가입 후 이용하실 수 있습니다.");
    }
}
