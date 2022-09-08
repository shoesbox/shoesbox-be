package com.shoesbox.domain.member;

import com.shoesbox.domain.auth.TokenRequestDto;
import com.shoesbox.domain.member.dto.MemberInfoUpdateDto;
import com.shoesbox.domain.member.dto.SignDto;
import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
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

    // 회원 정보 가져오기(기본값: 현재 로그인한 사용자의 정보 반환)
    @GetMapping("/info")
    public ResponseEntity<Object> getMemberInfo(@RequestParam(value = "m", defaultValue = "0") long targetId) {
        long memberId = SecurityUtil.getCurrentMemberIdByLong();
        if (targetId == 0L) {
            return ResponseEntity.ok(memberService.getMemberInfo(memberId, memberId));
        }
        return ResponseEntity.ok(memberService.getMemberInfo(memberId, targetId));
    }

    // 회원 정보 수정
    @PatchMapping("/info")
    public ResponseEntity<Object> updateMemberInfo(@RequestParam(value = "m", defaultValue = "0") long targetId, MemberInfoUpdateDto memberInfoUpdateDto) {
        long memberId = SecurityUtil.getCurrentMemberIdByLong();
        if (memberId != targetId) {
            throw new UnAuthorizedException("수정 권한이 없습니다.");
        }
        return ResponseEntity.ok(memberService.updateMemberInfo(memberId, memberInfoUpdateDto));
    }

    // 로그아웃
    @GetMapping("/logout")
    public ResponseEntity<Object> logout() {
        long memberId = SecurityUtil.getCurrentMemberIdByLong();
        return ResponseEntity.ok(memberService.logout(memberId));
    }

    // 회원 탈퇴
    @PostMapping("/delete")
    public ResponseEntity<Object> deleteAccount(@RequestParam(value = "m") long targetId) {
        long currentMemberId = SecurityUtil.getCurrentMemberIdByLong();
        if (currentMemberId != targetId) {
            throw new UnAuthorizedException("본인의 memberId가 아닙니다.");
        }
        return ResponseEntity.ok(memberService.deleteAccount(targetId));
    }
}
