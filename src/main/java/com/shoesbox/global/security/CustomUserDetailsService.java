package com.shoesbox.global.security;

import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String username) {
        return memberRepository.findByEmail(username)
                .map(this::createUser)
                .orElseThrow(() -> new UsernameNotFoundException(
                        username +
                                "이 DB에 존재하지 않습니다.")
                );
    }

    // DB 에 User 값이 존재한다면 UserDetails 객체로 만들어서 리턴
    private CustomUserDetails createUser(Member member) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(member.getAuthority());

        return CustomUserDetails.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .nickname(member.getNickname())
                .memberId(member.getId())
                .authorities(Collections.singleton(grantedAuthority))
                .build();
    }
}
