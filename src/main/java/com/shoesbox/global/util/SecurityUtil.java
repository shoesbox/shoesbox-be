package com.shoesbox.global.util;

import com.shoesbox.domain.auth.CustomUserDetails;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
@NoArgsConstructor
public class SecurityUtil {
    public static long getCurrentMemberId() {
        var principal = getPrincipal();

        if (principal == null) {
            throw new UnAuthorizedException("Security Context에 인증 정보가 없습니다.");
        }

        return principal.getMemberId();
    }

    public static String getCurrentMemberNickname() {
        var principal = getPrincipal();

        if (principal == null) {
            throw new UnAuthorizedException("Security Context에 인증 정보가 없습니다.");
        }

        return principal.getNickname();
    }

    private static CustomUserDetails getPrincipal() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.debug("Security Context에 인증 정보가 없습니다.");
            throw new UnAuthorizedException(
                    "Security Context에 인증 정보가 없습니다.");
        }

        if (authentication.getPrincipal() instanceof UserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }

        return null;
    }
}
