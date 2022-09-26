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

@Slf4j
@RequiredArgsConstructor
@RequestMapping(("/api/members"))
@RestController
public class GuestController {
    @Value("${spring.guest.guest_id}")
    private String GUEST_ID;
    @Value("${spring.guest.guest_email}")
    private String GUEST_EMAIL;
    @Value("${spring.guest.guest_password}")
    private String GUEST_PASSWORD;
    @Value("${spring.guest.friend_id_full}")
    private String FRIEND;
    @Value("${spring.guest.requested_friend_id_full}")
    private String REQUESTED_FRIEND;

    private final RedisTemplate<String, String> redisTemplate;

    private final GuestService guestService;
    private final MemberService memberService;

    @PostMapping("/auth/login/guest")
    public ResponseEntity<Object> login() {
        // 스케쥴러를 사용해 일정 시간마다(ex:3AM) 게스트 계정 삭제 필요
        // 추후 모든 게스트계정이 사용중일 경우도 추가 필요

        SignDto signDto = null;
        for (int i = 1; i <= 100; i++) {
            // 게스트는 총 100명까지 로그인 가능
            String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + GUEST_ID + i + "@" + GUEST_EMAIL);

            if (savedRefreshToken == null) {
                // 해당 게스트 아이디가 로그인되어있지 않을 경우
                signDto = SignDto.builder()
                        .email(GUEST_ID + i + "@" + GUEST_EMAIL)
                        .password(GUEST_PASSWORD)
                        .build();

                if (!guestService.isJoinedGuest(GUEST_ID + i + "@" + GUEST_EMAIL)) {
                    // 등록된 계정이 없을 경우, 새 계정 생성
                    memberService.signUp(signDto);
                    log.info(">>>>>>> 신규 게스트 계정 생성 : " + signDto.getEmail());

                    // 친구 관계 설정:친구 수락 관계
                    String friend = guestService.makeFriendToGuest(FRIEND, signDto.getEmail(), FriendState.FRIEND);
                    log.info(">>>>>>> " + friend);
                    // 친구 관계 설정:친구 요청 관계
                    String requestedFriend = guestService.makeFriendToGuest(REQUESTED_FRIEND, signDto.getEmail(), FriendState.REQUEST);
                    log.info(">>>>>>> " + requestedFriend);
                }

                // 등록된 계정이 있을 경우, 반복문 종료 후 로그인 처리
                break;
            }
        }
        log.info(">>>>>>> 게스트 계정 로그인 : " + signDto.getEmail());
        return ResponseHandler.ok(memberService.login(signDto));
    }
}
