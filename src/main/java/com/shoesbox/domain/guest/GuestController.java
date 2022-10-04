package com.shoesbox.domain.guest;

import com.shoesbox.domain.friend.FriendState;
import com.shoesbox.domain.member.MemberService;
import com.shoesbox.domain.member.dto.SignDto;
import com.shoesbox.global.common.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(("/api/members"))
@RestController
public class GuestController {
    @Value("guest")
    private String GUEST_ID;
    @Value("shoesboxguest.email")
    private String GUEST_EMAIL;
    @Value("1234")
    private String GUEST_PASSWORD;
    @Value("Friend2@test.com")
    private String REQUESTED_FRIEND;
    private final List<String> admins = Arrays.asList(
            "ipaper491@gmail.com",
            "parkhj929@kakao.com",
            "guuto9@gmail.com",
            "moungbak421@daum.net",
            "ciy1101@nate.com"
    );
    private final RedisTemplate<String, String> redisTemplate;

    private final GuestService guestService;
    private final MemberService memberService;

    @PostMapping("/auth/login/guest")
    public ResponseEntity<Object> login() {
        SignDto signDto = null;
        int guestCount = guestService.guestCount();

        for (int i = 0; i <= guestCount; i++) {
            String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + GUEST_ID + (i + 1) + "@" + GUEST_EMAIL);

            if (savedRefreshToken == null) {
                // 해당 게스트 아이디가 로그인되어있지 않을 경우
                signDto = SignDto.builder()
                        .email(GUEST_ID + (i + 1) + "@" + GUEST_EMAIL)
                        .password(GUEST_PASSWORD)
                        .build();

                if (!guestService.isJoinedGuest(GUEST_ID + (i + 1) + "@" + GUEST_EMAIL)) {
                    // 등록된 계정이 없을 경우, 새 계정 생성
                    memberService.signUp(signDto);
                    log.info("<<체험계정>> 생성 : " + signDto.getEmail());

                    // 새 게스트 계정의 친구 관계 설정
                    for (int j = 0; j < admins.size(); j++) {
                        guestService.makeFriendToGuest(admins.get(j), signDto.getEmail(), FriendState.FRIEND);
                    }
                    guestService.makeFriendToGuest(REQUESTED_FRIEND, signDto.getEmail(), FriendState.REQUEST);
                }

                // 등록된 계정이 있을 경우, 반복문 종료 후 로그인 처리
                break;
            }
        }

        log.info("<<체험계정>> 로그인 : " + signDto.getEmail());
        return ResponseHandler.ok(memberService.login(signDto));
    }
}
