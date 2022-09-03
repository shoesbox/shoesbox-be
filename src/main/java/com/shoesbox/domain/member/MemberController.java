package com.shoesbox.domain.member;

import com.shoesbox.domain.auth.TokenRequestDto;
import com.shoesbox.domain.member.dto.SignDto;
import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping(("/api/members"))
public class MemberController {
    private final MemberService memberService;

    // 회원 가입
    @PostMapping("/auth/signup")
    public ResponseEntity<Object> signUp(@RequestBody @Valid SignDto signDto) {
        return ResponseHandler.ok(memberService.signUp(signDto));
    }

    // 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<Object> login(@RequestBody @Valid SignDto signDto) {
        return ResponseHandler.ok(memberService.login(signDto));
    }

    // 토큰 재발급
    @PostMapping("/renew")
    public ResponseEntity<Object> renewToken(@RequestBody TokenRequestDto tokenRequestDto) {
        return ResponseHandler.ok(memberService.renewToken(tokenRequestDto));
    }

    // 내 정보 가져오기
    @GetMapping("/myinfo")
    public ResponseEntity<Object> getMyInfo() {
        var userId = SecurityUtil.getCurrentMemberIdByLong();
        return ResponseEntity.ok(memberService.getUserInfo(userId));
    }
}
