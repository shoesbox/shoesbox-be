package com.shoesbox.domain.member;

import com.shoesbox.domain.auth.TokenDto;
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

    @PostMapping("/auth/signup")
    public ResponseEntity<Object> signUp(@RequestBody @Valid SignDto signDto) {
        return ResponseHandler.ok(memberService.signUp(signDto));
    }

    // 로그인 요청
    @PostMapping("/auth/login")
    public ResponseEntity<TokenDto> login(@RequestBody @Valid SignDto signDto) {
        return ResponseEntity.ok(memberService.login(signDto));
    }

    // 내 정보 가져오기
    @GetMapping("/myinfo")
    public ResponseEntity<Object> getMyInfo() {
        var userId = SecurityUtil.getCurrentMemberIdByLong();
        return ResponseEntity.ok(memberService.getUserInfo(userId));
    }
}
