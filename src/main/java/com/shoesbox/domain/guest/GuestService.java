package com.shoesbox.domain.guest;

import com.shoesbox.domain.friend.Friend;
import com.shoesbox.domain.friend.FriendRepository;
import com.shoesbox.domain.friend.FriendState;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.global.exception.runtime.EntityNotFoundException;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GuestService {
    private static final String GUEST_ID = "guest";
    private static final String GUEST_EMAIL = "shoesboxguest.email";
    private final List<String> admins = Arrays.asList(
            "ipaper491@gmail.com",
            "parkhj929@kakao.com",
            "guuto9@gmail.com",
            "moungbak421@daum.net",
            "ciy1101@nate.com"
    );
    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    @Value("${default-images.profile}")
    private String DEFAULT_PROFILE_URL;

    @Transactional(readOnly = true)
    public void guestCheck(long currentMemberId) {
        Member currentMember = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new EntityNotFoundException(Member.class.getPackageName()));
        boolean isGuest = currentMember.getEmail().contains(GUEST_ID) && currentMember.getEmail().contains(GUEST_EMAIL);

        if (isGuest) {
            throw new UnAuthorizedException("체험용 계정입니다. 회원가입 후 이용하실 수 있습니다.");
        }
    }

    @Transactional(readOnly = true)
    public int guestCount() {
        return memberRepository.findAllByEmailEndsWith(GUEST_EMAIL).size();
    }

    @Transactional(readOnly = true)
    public boolean isJoinedGuest(String guestEmail) {
        return memberRepository.existsByEmail(guestEmail);
    }

    @Transactional
    public void makeFriendToGuest(String fromMemberEmail, String toMemberEmail, FriendState friendState) {
        Member fromMember = memberRepository.findByEmail(fromMemberEmail).orElse(null);

        // 친구계정이 관리자가 아니고, db에 없을 경우 생성
        if (fromMember == null && !admins.contains(fromMemberEmail)) {
            fromMember = makeFriendAccount(fromMemberEmail);
        }

        Member toMember = memberRepository.findByEmail(toMemberEmail)
                .orElseThrow(() -> new IllegalArgumentException("게스트 계정 아이디를 찾을 수 없습니다."));

        Friend friend = Friend.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .friendState(friendState)
                .build();

        friendRepository.save(friend);
    }

    @Transactional
    public Member makeFriendAccount(String friendEmail) {
        Member friendMember = Member.builder()
                .email(friendEmail)
                .password(bCryptPasswordEncoder.encode("1234"))
                .nickname(friendEmail.split("@")[0])
                .profileImageUrl(DEFAULT_PROFILE_URL)
                .build();

        memberRepository.save(friendMember);
        log.info("<< GUEST SERVICE >> 친구 계정 생성");

        return friendMember;
    }

}
