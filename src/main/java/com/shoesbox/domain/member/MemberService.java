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
import com.shoesbox.domain.sse.Alarm;
import com.shoesbox.domain.sse.AlarmRepository;
import com.shoesbox.global.config.jwt.JwtExceptionCode;
import com.shoesbox.global.config.jwt.JwtProvider;
import com.shoesbox.global.exception.runtime.EntityNotFoundException;
import com.shoesbox.global.exception.runtime.InvalidJwtException;
import com.shoesbox.global.exception.runtime.RefreshTokenNotFoundException;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import com.shoesbox.global.util.ImageUtil;
import com.shoesbox.global.util.S3Util;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//import static com.shoesbox.domain.sse.SseController.sseEmitters;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    @Value("${default-images.profile}")
    private String DEFAULT_PROFILE_IMAGE_URL;
    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtProvider jwtProvider;
    private final S3Util s3Util;
    private final RedisService redisService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ImageUtil imageUtil;
    private final AlarmRepository alarmRepository;

    @Transactional
    public String signUp(SignDto signDto) {
        checkEmail(signDto.getEmail());
        return memberRepository.save(toMember(signDto))
                .getNickname();
    }

    @Transactional
    public TokenResponseDto login(SignDto signDto) {
        // Login ???????????? ?????? ?????? email/pw ??? ???????????? AuthenticationToken ??????
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                signDto.getEmail(), signDto.getPassword());

        // ????????? ?????? (????????? ???????????? ??????) ??? ??????????????? ??????
        // authenticate ???????????? ????????? ??? ??? CustomUserDetailsService ?????? ???????????? loadUserByUsername ???????????? ?????????
        Authentication authentication;
        try {
            authentication = authenticationManagerBuilder.getObject()
                    .authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("?????????, ?????? ??????????????? ?????????????????????.");
        }

        // ?????? ????????? ????????? JWT ?????? ??????
        TokenResponseDto tokenResponseDto = jwtProvider.createTokenDto(
                (CustomUserDetails) authentication.getPrincipal());

        // RefreshToken ??????
        String refreshToken = tokenResponseDto.getRefreshToken();
        redisService.setDataWithExpiration("RT:" + authentication.getName(), refreshToken,
                tokenResponseDto.getRefreshTokenLifetimeInMs());

        // ?????? ??????
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
        // ????????? ????????? ???????????? ?????????
        if (memberInfoUpdateDto.getImageFile() != null && !memberInfoUpdateDto.getImageFile().isEmpty()) {
            // ?????? ????????? ?????? ??????
            var deleteRequest = s3Util.createDeleteRequest(member.getProfileImageUrl());
            // ????????? ???????????? WebP??? ??????
            var createdImageFile = imageUtil.convertToWebp(memberInfoUpdateDto.getImageFile());
            // ???????????? ??? ?????? ??????
            var thumbnail = imageUtil.resizeImage(createdImageFile);
            createdImageFile.delete();
            // ????????? ??????
            var putRequest = s3Util.createPutObjectRequest(thumbnail);
            // ?????????, ?????? ?????? ??????
            profileImageUrl = s3Util.executePutRequest(putRequest);
            s3Util.executeDeleteRequest(deleteRequest);
        }

        member.updateInfo(memberInfoUpdateDto.getNickname(), profileImageUrl);
        return currentMemberId;
    }

    @Transactional
    public TokenResponseDto refreshToken(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token ??????
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

        // 2. Access Token ?????? memberId(PK) ????????????
        Authentication authentication = jwtProvider.getAuthentication(tokenRequestDto.getAccessToken());
        var userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 3. (??????) Redis ??????????????? ?????? ????????????????????? ??????
        String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + authentication.getName());
        if (savedRefreshToken == null) {
            throw new RefreshTokenNotFoundException("???????????? ??? ??????????????????.");
        }

        // 4. Refresh Token ??????????????? ?????? (??????) ???????????? ????????? ??????)
        if (!savedRefreshToken.equals(tokenRequestDto.getRefreshToken())) {
            throw new InvalidJwtException("????????? ?????? ????????? ???????????? ????????????.");
        }

        // 5. Access Token ?????? ????????? memberId(PK)??? ?????? ????????? ????????? ???????????? ?????? ?????? ??????
        TokenResponseDto refreshedTokenResponseDto = jwtProvider.createTokenDto(userDetails);

        // 6. db??? ???????????? ?????? ?????? ???????????? -> Redis??? Refresh ????????????
        redisService.setDataWithExpiration("RT:" + authentication.getName(),
                refreshedTokenResponseDto.getRefreshToken(),
                refreshedTokenResponseDto.getRefreshTokenLifetimeInMs());

        // ?????? ??????
        return refreshedTokenResponseDto;
    }

    @Transactional
    public Boolean logout(String email) {
//        Member member = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new EntityNotFoundException(Member.class.getPackageName()));
//        sseEmitters.remove(member.getId());
        return redisTemplate.delete("RT:" + email);
    }

    @Transactional
    public long resetProfileImage(long currentMemberId) {
        var member = getMember(currentMemberId);
        var deleteRequest = s3Util.createDeleteRequest(member.getProfileImageUrl());
        s3Util.executeDeleteRequest(deleteRequest);
        member.updateInfo(member.getNickname(), DEFAULT_PROFILE_IMAGE_URL);
        return member.getId();
    }

    @Transactional
    public long deleteAccount(long targetId) {
        var member = getMember(targetId);
        memberRepository.delete(member);
        redisTemplate.delete("RT:" + member.getEmail());

        // ?????? ?????? ?????? ??????
        List<Alarm> alarms = alarmRepository.findAllByReceiverMemberId(targetId);
        alarmRepository.deleteAll(alarms);

        return targetId;
    }

    private Member getMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(Member.class.getPackageName()));
    }

    private void checkEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateUserInfoException("???????????? ??????????????????. ????????? ????????????.");
        }
    }

    private Member toMember(SignDto signDto) {
        return Member.builder()
                .email(signDto.getEmail())
                .password(bCryptPasswordEncoder.encode(signDto.getPassword()))
                .nickname(signDto.getEmail().split("@")[0])
                .profileImageUrl(DEFAULT_PROFILE_IMAGE_URL)
                .build();
    }

    // ?????? ??????, ?????? ?????? ???????????? ??????
    private void checkAuthorization(long currentMemberId, long targetId) {
        checkSelfAuthorization(currentMemberId, targetId);
        if (!isFriend(currentMemberId, targetId)) {
            throw new UnAuthorizedException("?????? ????????? ????????????.");
        }
    }

    // ????????? memberId(targetId)??? ?????? ????????? ??? ???????????? memberId??? ???????????? ??????
    private void checkSelfAuthorization(long currentMemberId, long targetId) {
        if (currentMemberId != targetId) {
            throw new UnAuthorizedException("?????? ????????? ????????????.");
        }
    }

    // ??? memberId??? ?????? ?????? ???????????? ??????
    private boolean isFriend(long currentMemberId, long targetId) {
        return friendRepository.existsByFromMemberIdAndToMemberIdAndFriendState(
                targetId, currentMemberId, FriendState.FRIEND)
                || friendRepository.existsByFromMemberIdAndToMemberIdAndFriendState(
                currentMemberId, targetId, FriendState.FRIEND);
    }
}
