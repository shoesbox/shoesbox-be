package com.shoesbox.global.util;

import com.shoesbox.global.security.CustomUserDetails;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
@NoArgsConstructor
public class SecurityUtil {
    public static long getCurrentMemberIdByLong() {
        var principal = getPrincipal();

        if (principal != null) {
            return principal.getMemberId();
        }

        return 0L;
    }

    public static String getCurrentMemberNickname() {
        var principal = getPrincipal();

        if (principal != null) {
            return principal.getNickname();
        }

        return null;
    }

    private static CustomUserDetails getPrincipal() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.debug("Security Context에 인증 정보가 없습니다.");
            throw new UsernameNotFoundException(
                    "Security Context에 인증 정보가 없습니다.");
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }
}
