package com.shoesbox.domain.guest;

import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.global.exception.runtime.EntityNotFoundException;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    public void guestCheck(long currentMemberId) {
        if (isGuest(currentMemberId)) throw new UnAuthorizedException("체험용 계정입니다. 회원가입 후 이용하실 수 있습니다.");
    }

    @Transactional(readOnly = true)
    public boolean isGuest(long currentMemberId) {
        Member currentMember = memberRepository.findById(currentMemberId).orElseThrow(() -> new EntityNotFoundException(
                Member.class.getPackageName()));

//        return currentMember.getEmail().equals("guest@test.com");
        return currentMember.getEmail().contains(guestId) && currentMember.getEmail().contains(testEmail);
    }
}
