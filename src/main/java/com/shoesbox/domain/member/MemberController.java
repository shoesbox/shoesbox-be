package com.shoesbox.domain.member;

import com.shoesbox.domain.member.dto.SignUpDto;
import com.shoesbox.global.common.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
@RequestMapping(("/api/members"))
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/auth/signup")
    public ResponseEntity<Object> signUp(@RequestBody @Valid SignUpDto signUpDto) {
        return ResponseHandler.ok(memberService.signUp(signUpDto));
    }
}
