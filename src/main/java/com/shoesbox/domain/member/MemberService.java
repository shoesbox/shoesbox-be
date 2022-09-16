package com.shoesbox.domain.member;

import com.shoesbox.domain.auth.TokenDto;
import com.shoesbox.domain.auth.TokenRequestDto;
import com.shoesbox.domain.auth.redis.RedisService;
import com.shoesbox.domain.member.dto.MemberInfoResponseDto;
import com.shoesbox.domain.member.dto.MemberInfoUpdateDto;
import com.shoesbox.domain.member.dto.SignDto;
import com.shoesbox.domain.photo.S3Service;
import com.shoesbox.global.exception.runtime.DuplicateUserInfoException;
import com.shoesbox.global.exception.runtime.InvalidJWTException;
import com.shoesbox.global.exception.runtime.RefreshTokenNotFoundException;
import com.shoesbox.global.security.CustomUserDetails;
import com.shoesbox.global.security.jwt.ExceptionCode;
import com.shoesbox.global.security.jwt.TokenProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
    private static final String BASE_PROFILE_IMAGE_URL = "https://i.ibb.co/N27FwdP/image.png";
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final S3Service s3Service;
    private final RedisService redisService;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public String signUp(SignDto signDto) {
        if (!checkEmail(signDto.getEmail())) {
            log.info("사용중인 이메일입니다. 로그인 해주세요.");
            throw new DuplicateUserInfoException("사용중인 이메일입니다. 로그인 해주세요.");
        }
        Member createdMember = memberRepository.save(toMember(signDto));

        return createdMember.getNickname();
    }

    @Transactional
    public TokenDto login(SignDto signDto) {
        // Login 화면에서 입력 받은 username/pw 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(signDto.getEmail(), signDto.getPassword());

        // 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        // authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication;
        try {
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            log.info("아이디, 혹은 비밀번호가 잘못되었습니다.");
            throw new BadCredentialsException("아이디, 혹은 비밀번호가 잘못되었습니다.");
        }

        // CustomUserDetails 생성
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 인증 정보를 사용해 JWT 토큰 생성
        TokenDto tokenDto = tokenProvider.createTokenDto(userDetails);

        // RefreshToken 저장
        String refreshToken = tokenDto.getRefreshToken();
        redisService.setDataWithExpiration("RT:" + authentication.getName(), refreshToken,
                tokenDto.getRefreshTokenLifetimeInMs());

        // 토큰 발급
        return tokenDto;
    }

    @Transactional(readOnly = true)
    public MemberInfoResponseDto getMemberInfo(long memberId) {
        Member targetMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("memberId: " + memberId + "는 존재하지 않습니다."));

        return MemberInfoResponseDto.builder()
                .memberId(memberId)
                .nickname(targetMember.getNickname())
                .email(targetMember.getEmail())
                .profileImageUrl(targetMember.getProfileImageUrl())
                .build();
    }

    @Transactional
    public MemberInfoResponseDto updateMemberInfo(long memberId, MemberInfoUpdateDto memberInfoUpdateDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("memberId: " + memberId + "는 존재하지 않습니다."));

        String savedUrl = member.getProfileImageUrl();
        if (memberInfoUpdateDto.getImageFile() != null && !memberInfoUpdateDto.getImageFile().isEmpty()) {
            savedUrl = s3Service.uploadImage(memberInfoUpdateDto.getImageFile());
        }

        member.updateInfo(memberInfoUpdateDto.getNickname(), savedUrl);

        return MemberInfoResponseDto.builder()
                .memberId(memberId)
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }

    @Transactional
    public TokenDto refreshToken(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token 검증
        try {
            tokenProvider.validateToken(tokenRequestDto.getRefreshToken());
        } catch (SecurityException | MalformedJwtException e) {
            log.info(ExceptionCode.INVALID_SIGNATURE_TOKEN.getMessage());
            throw new InvalidJWTException(ExceptionCode.INVALID_SIGNATURE_TOKEN.getMessage());
        } catch (ExpiredJwtException e) {
            log.info(ExceptionCode.EXPIRED_TOKEN.getMessage());
            throw new InvalidJWTException(ExceptionCode.EXPIRED_TOKEN.getMessage());
        } catch (UnsupportedJwtException e) {
            log.info(ExceptionCode.UNSUPPORTED_TOKEN.getMessage());
            throw new InvalidJWTException(ExceptionCode.UNSUPPORTED_TOKEN.getMessage());
        } catch (IllegalArgumentException e) {
            log.info(ExceptionCode.WRONG_TOKEN.getMessage());
            throw new InvalidJWTException(ExceptionCode.WRONG_TOKEN.getMessage());
        } catch (Exception e) {
            log.info(ExceptionCode.UNKNOWN_ERROR.getMessage());
            throw new InvalidJWTException(ExceptionCode.UNKNOWN_ERROR.getMessage());
        }

        // 2. Access Token 에서 memberId(PK) 가져오기
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());
        var userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 3. (수정) Redis 저장소에서 토큰 가져오는것으로 대체
        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + authentication.getName());
        if (savedRefreshToken == null) {
            throw new RefreshTokenNotFoundException("로그아웃 된 사용자입니다.");
        }

        // 4. Refresh Token 일치하는지 검사 (추가) 로그아웃 사용자 검증)
        if (!savedRefreshToken.equals(tokenRequestDto.getRefreshToken())) {
            throw new InvalidJWTException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. Access Token 에서 가져온 memberId(PK)를 다시 새로운 토큰의 클레임에 넣고 토큰 생성
        TokenDto refreshedTokenDto = tokenProvider.createTokenDto(userDetails);

        // 6. db의 리프레쉬 토큰 정보 업데이트 -> Redis에 Refresh 업데이트
        redisService.setDataWithExpiration(
                "RT:" + authentication.getName(),
                refreshedTokenDto.getRefreshToken(),
                refreshedTokenDto.getRefreshTokenLifetimeInMs());

        // 토큰 발급
        return refreshedTokenDto;
    }

    @Transactional
    public TokenDto logout(String email) {
        redisTemplate.delete("RT:" + email);
        return tokenProvider.createEmptyTokenDto();
    }

    @Transactional
    public MemberInfoResponseDto resetProfileImage(long currentMemberId) {
        var currentMember = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new UsernameNotFoundException("memberId: " + currentMemberId + "는 존재하지 않습니다."));

        currentMember.updateInfo(currentMember.getNickname(), BASE_PROFILE_IMAGE_URL);

        return MemberInfoResponseDto.builder()
                .memberId(currentMemberId)
                .nickname(currentMember.getNickname())
                .email(currentMember.getEmail())
                .profileImageUrl(currentMember.getProfileImageUrl())
                .build();
    }

    public long deleteAccount(long targetId) {
        var member = memberRepository.findById(targetId)
                .orElseThrow(
                        () -> new UsernameNotFoundException("memberId: " + targetId + "는 존재하지 않습니다.")
                );
        memberRepository.delete(member);

        return targetId;
    }

    private boolean checkEmail(String email) {
        return !memberRepository.existsByEmail(email);
    }

    private Member toMember(SignDto signDto) {
        return Member.builder()
                .email(signDto.getEmail())
                .password(bCryptPasswordEncoder.encode(signDto.getPassword()))
                .nickname(signDto.getEmail().split("@")[0])
                .profileImageUrl("https://i.ibb.co/N27FwdP/image.png")
                .build();
    }
}
