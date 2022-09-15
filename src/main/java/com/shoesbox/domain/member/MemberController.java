package com.shoesbox.domain.member;

import com.shoesbox.domain.auth.TokenRequestDto;
import com.shoesbox.domain.friend.FriendService;
import com.shoesbox.domain.member.dto.MemberInfoUpdateDto;
import com.shoesbox.domain.member.dto.SignDto;
import com.shoesbox.global.common.ResponseHandler;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import com.shoesbox.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping(("/api/members"))
public class MemberController {
    private final MemberService memberService;
    private final FriendService friendService;

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
    @PostMapping("/refresh")
    public ResponseEntity<Object> refreshToken(@RequestBody TokenRequestDto tokenRequestDto) {
        return ResponseHandler.ok(memberService.refreshToken(tokenRequestDto));
    }

    // 회원 정보 가져오기(기본값: 현재 로그인한 사용자의 정보 반환)
    @GetMapping("/info")
    public ResponseEntity<Object> getMemberInfo(@RequestParam(value = "m", defaultValue = "0") long targetId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (targetId == 0L) {
            targetId = currentMemberId;
        }
        if (targetId != currentMemberId && !friendService.isFriend(targetId, currentMemberId)) {
            throw new UnAuthorizedException("접근 권한이 없습니다.");
        }

        return ResponseHandler.ok(memberService.getMemberInfo(targetId));
    }

    // 회원 정보 수정
    @PatchMapping("/info")
    public ResponseEntity<Object> updateMemberInfo(
            @RequestParam(value = "m", defaultValue = "0") long targetId, MemberInfoUpdateDto memberInfoUpdateDto) {
        long memberId = SecurityUtil.getCurrentMemberId();
        if (targetId == 0L || targetId == memberId) {
            return ResponseEntity.ok(memberService.updateMemberInfo(memberId, memberInfoUpdateDto));
        }
        throw new UnAuthorizedException("수정 권한이 없습니다.");
    }

    // 로그아웃
    @GetMapping("/logout")
    public ResponseEntity<Object> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseHandler.ok(memberService.logout(authentication.getName()));
    }

    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteAccount(@RequestParam(value = "m") long targetId) {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId != targetId) {
            throw new UnAuthorizedException("본인의 memberId가 아닙니다.");
        }
        return ResponseHandler.ok(memberService.deleteAccount(targetId));
    }

    // 프로필 사진 초기화
    @GetMapping("/reset")
    public ResponseEntity<Object> resetProfileImage() {
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        return ResponseHandler.ok(memberService.resetProfileImage(currentMemberId));
    }
}
