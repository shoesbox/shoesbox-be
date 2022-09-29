package com.shoesbox.domain.member;

import com.shoesbox.domain.auth.CustomUserDetails;
import com.shoesbox.domain.auth.RedisService;
import com.shoesbox.domain.auth.dto.TokenRequestDto;
import com.shoesbox.domain.auth.dto.TokenResponseDto;
import com.shoesbox.domain.friend.FriendRepository;
import com.shoesbox.domain.friend.FriendState;
import com.shoesbox.domain.member.dto.MemberInfoResponseDto;
import com.shoesbox.domain.member.dto.MemberInfoUpdateDto;
import com.shoesbox.domain.member.dto.SignDto;
import com.shoesbox.domain.member.exception.DuplicateUserInfoException;
import com.shoesbox.domain.photo.S3Service;
import com.shoesbox.global.config.jwt.JwtExceptionCode;
import com.shoesbox.global.config.jwt.JwtProvider;
import com.shoesbox.global.exception.runtime.EntityNotFoundException;
import com.shoesbox.global.exception.runtime.InvalidJwtException;
import com.shoesbox.global.exception.runtime.RefreshTokenNotFoundException;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.shoesbox.domain.sse.SseController.sseEmitters;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    private static final String BASE_PROFILE_IMAGE_URL = "https://i.ibb.co/N27FwdP/image.png";
    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtProvider jwtProvider;
    private final S3Service s3Service;
    private final RedisService redisService;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public String signUp(SignDto signDto) {
        checkEmail(signDto.getEmail());
        return memberRepository.save(toMember(signDto)).getNickname();
    }

    @Transactional
    public TokenResponseDto login(SignDto signDto) {
        // Login 화면에서 입력 받은 email/pw 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(signDto.getEmail(), signDto.getPassword());

        // 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        // authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication;
        try {
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("아이디, 혹은 비밀번호가 잘못되었습니다.");
        }

        // 인증 정보를 사용해 JWT 토큰 생성
        TokenResponseDto tokenResponseDto = jwtProvider.createTokenDto(
                (CustomUserDetails) authentication.getPrincipal());

        // RefreshToken 저장
        String refreshToken = tokenResponseDto.getRefreshToken();
        redisService.setDataWithExpiration("RT:" + authentication.getName(), refreshToken,
                tokenResponseDto.getRefreshTokenLifetimeInMs());

        // 토큰 발급
        return tokenResponseDto;
    }

    @Transactional(readOnly = true)
    public MemberInfoResponseDto getMemberInfo(long currentMemberId, long targetId) {
        if (currentMemberId != targetId) {
            checkAuthorization(currentMemberId, targetId);
        } else {
            checkSelfAuthorization(currentMemberId, targetId);
        }
        Member member = getMember(targetId);
        return MemberInfoResponseDto.builder()
                .memberId(targetId)
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }

    @Transactional
    public long updateMemberInfo(long currentMemberId, long targetId, MemberInfoUpdateDto memberInfoUpdateDto) {
        checkSelfAuthorization(currentMemberId, targetId);
        Member member = getMember(currentMemberId);
        String profileImageUrl = member.getProfileImageUrl();
        // 변경할 프로필 이미지가 있으면
        if (memberInfoUpdateDto.getImageFile() != null && !memberInfoUpdateDto.getImageFile().isEmpty()) {
            // 기존 이미지 삭제하고
            s3Service.deleteObjectByImageUrl(member.getProfileImageUrl());
            // 새로운 이미지 업로드
            profileImageUrl = s3Service.uploadImage(memberInfoUpdateDto.getImageFile());
        }
        member.updateInfo(memberInfoUpdateDto.getNickname(), profileImageUrl);

        return currentMemberId;
    }

    @Transactional
    public TokenResponseDto refreshToken(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token 검증
        try {
            jwtProvider.validateToken(tokenRequestDto.getRefreshToken());
        } catch (SecurityException | MalformedJwtException e) {
            log.info(JwtExceptionCode.INVALID_SIGNATURE_TOKEN.getMessage());
            throw new InvalidJwtException(JwtExceptionCode.INVALID_SIGNATURE_TOKEN.getMessage());
        } catch (ExpiredJwtException e) {
            log.info(JwtExceptionCode.EXPIRED_TOKEN.getMessage());
            throw new InvalidJwtException(JwtExceptionCode.EXPIRED_TOKEN.getMessage());
        } catch (UnsupportedJwtException e) {
            log.info(JwtExceptionCode.UNSUPPORTED_TOKEN.getMessage());
            throw new InvalidJwtException(JwtExceptionCode.UNSUPPORTED_TOKEN.getMessage());
        } catch (IllegalArgumentException e) {
            log.info(JwtExceptionCode.WRONG_TOKEN.getMessage());
            throw new InvalidJwtException(JwtExceptionCode.WRONG_TOKEN.getMessage());
        } catch (Exception e) {
            log.info(JwtExceptionCode.UNKNOWN_ERROR.getMessage());
            throw new InvalidJwtException(JwtExceptionCode.UNKNOWN_ERROR.getMessage());
        }

        // 2. Access Token 에서 memberId(PK) 가져오기
        Authentication authentication = jwtProvider.getAuthentication(tokenRequestDto.getAccessToken());
        var userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 3. (수정) Redis 저장소에서 토큰 가져오는것으로 대체
        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + authentication.getName());
        if (savedRefreshToken == null) {
            throw new RefreshTokenNotFoundException("로그아웃 된 사용자입니다.");
        }

        // 4. Refresh Token 일치하는지 검사 (추가) 로그아웃 사용자 검증)
        if (!savedRefreshToken.equals(tokenRequestDto.getRefreshToken())) {
            throw new InvalidJwtException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. Access Token 에서 가져온 memberId(PK)를 다시 새로운 토큰의 클레임에 넣고 토큰 생성
        TokenResponseDto refreshedTokenResponseDto = jwtProvider.createTokenDto(userDetails);

        // 6. db의 리프레쉬 토큰 정보 업데이트 -> Redis에 Refresh 업데이트
        redisService.setDataWithExpiration(
                "RT:" + authentication.getName(),
                refreshedTokenResponseDto.getRefreshToken(),
                refreshedTokenResponseDto.getRefreshTokenLifetimeInMs());

        // 토큰 발급
        return refreshedTokenResponseDto;
    }

    @Transactional
    public Boolean logout(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("로그아웃 할 유저를 찾을 수 없습니다."));
        if (sseEmitters.containsKey(member.getId())) {
            sseEmitters.remove(member.getId());
        }
        return redisTemplate.delete("RT:" + email);
    }

    @Transactional
    public MemberInfoResponseDto resetProfileImage(long currentMemberId) {
        var member = getMember(currentMemberId);
        member.updateInfo(member.getNickname(), BASE_PROFILE_IMAGE_URL);
        return MemberInfoResponseDto.builder()
                .memberId(currentMemberId)
                .nickname(member.getNickname())
                .email(member.getEmail())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }

    @Transactional
    public long deleteAccount(long targetId) {
        var member = getMember(targetId);
        memberRepository.delete(member);
        redisTemplate.delete("RT:" + member.getEmail());
        return targetId;
    }

    private Member getMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(Member.class.getPackageName()));
    }

    private void checkEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateUserInfoException("사용중인 이메일입니다. 로그인 해주세요.");
        }
    }

    private Member toMember(SignDto signDto) {
        return Member.builder()
                .email(signDto.getEmail())
                .password(bCryptPasswordEncoder.encode(signDto.getPassword()))
                .nickname(signDto.getEmail().split("@")[0])
                .profileImageUrl("https://i.ibb.co/N27FwdP/image.png")
                .build();
    }

    // 자기 자신, 혹은 친구 관계인지 검증
    private void checkAuthorization(long currentMemberId, long targetId) {
        checkSelfAuthorization(currentMemberId, targetId);
        if (!isFriend(currentMemberId, targetId)) {
            throw new UnAuthorizedException("접근 권한이 없습니다.");
        }
    }

    // 요청한 memberId(targetId)와 현재 로그인 한 사용자의 memberId가 동일한지 검증
    private void checkSelfAuthorization(long currentMemberId, long targetId) {
        if (currentMemberId != targetId) {
            throw new UnAuthorizedException("접근 권한이 없습니다.");
        }
    }

    // 두 memberId가 서로 친구 관계인지 검증
    private boolean isFriend(long currentMemberId, long targetId) {
        return friendRepository.existsByFromMemberIdAndToMemberIdAndFriendState(
                targetId, currentMemberId, FriendState.FRIEND) ||
                friendRepository.existsByFromMemberIdAndToMemberIdAndFriendState(
                        currentMemberId, targetId, FriendState.FRIEND);
    }
}
