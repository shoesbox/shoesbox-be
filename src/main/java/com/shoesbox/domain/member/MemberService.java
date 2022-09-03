package com.shoesbox.domain.member;

import com.shoesbox.domain.auth.RefreshToken;
import com.shoesbox.domain.auth.RefreshTokenRepository;
import com.shoesbox.domain.auth.TokenDto;
import com.shoesbox.domain.member.dto.MemberInfoDto;
import com.shoesbox.domain.member.dto.SignDto;
import com.shoesbox.global.exception.runtime.DuplicateUserInfoException;
import com.shoesbox.global.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;

    @Transactional
    public String signUp(SignDto signDto) {
        if (!checkEmail(signDto.getEmail())) {
            log.info("이미 가입되어 있는 유저입니다");
            throw new DuplicateUserInfoException("이미 가입되어 있는 유저입니다");
        }

        Member createdMember = memberRepository.save(toMember(signDto));
        return createdMember.getNickname();
    }

    @Transactional
    public TokenDto login(SignDto signDto) {
        // 1. Login 화면에서 입력 받은 username/pw 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(signDto.getEmail(), signDto.getPassword());

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication;
        try {
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            log.info("아이디, 혹은 비밀번호가 잘못되었습니다.");
            throw new BadCredentialsException("아이디, 혹은 비밀번호가 잘못되었습니다.");
        }

        long userId = memberRepository.findByEmail(authentication.getName())
                .map(Member::getId)
                .orElseThrow(
                        () -> new UsernameNotFoundException("아이디를 찾을 수 없습니다.")
                );

        // 3. 인증 정보를 사용해 JWT 토큰 생성
        TokenDto tokenDto = tokenProvider.createTokenDto(authentication, userId);

        // 5. RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenValue(tokenDto.getRefreshToken())
                .build();
        refreshTokenRepository.save(refreshToken);

        // 6. 토큰 발급
        return tokenDto;
    }

    @Transactional(readOnly = true)
    public MemberInfoDto getUserInfo(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(
                        () -> new UsernameNotFoundException("userId: " + userId + "는 존재하지 않는 아이디입니다.")
                );
        return MemberInfoDto.builder()
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .selfDescription(member.getSelfDescription())
                .build();
    }

    public boolean checkEmail(String email) {
        return !memberRepository.existsByEmail(email);
    }

    private Member toMember(SignDto signDto) {
        return Member.builder()
                .email(signDto.getEmail())
                .password(bCryptPasswordEncoder.encode(signDto.getPassword()))
                .nickname(signDto.getEmail().split("@")[0])
                .profileImageUrl("Url")
                .selfDescription("Hi")
                .build();
    }
}
