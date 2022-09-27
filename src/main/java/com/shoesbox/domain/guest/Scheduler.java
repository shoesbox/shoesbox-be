package com.shoesbox.domain.guest;

import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class Scheduler {

    @Value("guest")
    private String GUEST_ID;
    @Value("shoesboxguest.email")
    private String GUEST_EMAIL;

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    // 초, 분, 시 일, 월, 주 : 매일 5시마다 게스트 계정 삭제
    @Scheduled(cron = "0 0 5 * * *")
    public void deleteGuestAccount() throws InterruptedException {
        // 1초에 1db씩 조회
        TimeUnit.SECONDS.sleep(1);
        List<Member> guestMember = memberRepository.findAllByEmailEndsWith(GUEST_EMAIL);

        for (int i = 0; i < guestMember.size(); i++) {
            memberService.logout(guestMember.get(i).getEmail());
            memberService.deleteAccount(guestMember.get(i).getId());
        }
        log.info("<<스케줄러>> 실행 : " + (guestMember.size() + "개 계정 삭제"));
    }
}
