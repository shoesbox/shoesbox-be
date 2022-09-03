package com.shoesbox.domain.member;

import com.shoesbox.domain.member.dto.SignUpDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public String signUp(SignUpDto signUpDto) {
        Member createdMember = memberRepository.save(toMember(signUpDto));
        return createdMember.getNickname();
    }

    private Member toMember(SignUpDto signUpDto) {
        return Member.builder()
                .email(signUpDto.getEmail())
                .password(bCryptPasswordEncoder.encode(signUpDto.getPassword()))
                .nickname(signUpDto.getEmail().split("@")[0])
                .profileImageUrl("Url")
                .selfDescription("Hi")
                .build();
    }
}
