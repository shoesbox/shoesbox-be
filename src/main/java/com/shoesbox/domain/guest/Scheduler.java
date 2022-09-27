package com.shoesbox.domain.guest;

import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.domain.member.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class Scheduler {

    @Value("guest")
    private String GUEST_ID;
    @Value("test.com")
    private String GUEST_EMAIL;

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    // 초, 분, 시 일, 월, 주 : 매일 5시마다 게스트 계정 삭제
    @Scheduled(cron = "0 0 5 * * *")
    public void deleteGuestAccount() throws InterruptedException {
        for (int i = 1; i <= 100; i++) {
            // 1초에 1db씩 조회
            TimeUnit.SECONDS.sleep(1);
            Member guestMember = memberRepository.findByEmail(GUEST_ID + i + "@" + GUEST_EMAIL).orElse(null);

            if (guestMember == null) {
                // 더이상 조회되는 guest계정이 없을 경우 종료
                log.info("<<스케줄러>> 실행 : guest" + i + " 부터 조회되는 계정 없음");
                break;
            } else {
                memberService.logout(GUEST_ID + i + "@" + GUEST_EMAIL);
                memberService.deleteAccount(guestMember.getId());
                log.info("<<스케줄러>> 실행 : " + guestMember.getNickname() + "계정 삭제");
            }
        }


    }
}
