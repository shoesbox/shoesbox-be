package com.shoesbox.global.security.jwt;

import com.shoesbox.global.exception.runtime.InvalidJWTException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.InvalidKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    private final TokenProvider tokenProvider;

    // 필터링 로직이 들어가는 곳
    // JWT 토큰의 인증 정보를 현재 쓰레드의 SecurityContext에 저장하는 역할 수행
    @Override
    public void doFilterInternal(HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse,
                                 FilterChain filterChain)
            throws IOException, ServletException {

        // 1. Request Header에서 토큰을 꺼낸다
        String jwt = resolveToken(servletRequest);
        // 요청이 들어온 URI
        String requestURI = servletRequest.getRequestURI();

        // 2. validateToken으로 토큰 유효성 검사
        try {
            // 토큰에 값이 들어 있으면
            if (StringUtils.hasText(jwt)) {
                // 유효성 검사
                tokenProvider.validateToken(jwt);
                // 정상 토큰이면 해당 토큰으로 Authentication(userId)을 가져와서
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                log.info("Authentication: " + authentication);
                // SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Security Context에 'userId: {}' 인증 정보를 저장했습니다.",
                        authentication.getName());
                log.info("uri: {}", requestURI);
            }
        } catch (SecurityException | MalformedJwtException e) {
            servletRequest.setAttribute("exception", ExceptionCode.INVALID_SIGNATURE_TOKEN.getCode());
        } catch (ExpiredJwtException e) {
            servletRequest.setAttribute("exception", ExceptionCode.EXPIRED_TOKEN.getCode());
        } catch (UnsupportedJwtException e) {
            servletRequest.setAttribute("exception", ExceptionCode.UNSUPPORTED_TOKEN.getCode());
        } catch (IllegalArgumentException e) {
            servletRequest.setAttribute("exception", ExceptionCode.WRONG_TOKEN.getCode());
        } catch (InvalidJWTException e) {
            servletRequest.setAttribute("exception", ExceptionCode.LOGGED_OUT_TOKEN.getCode());
        } catch (InvalidKeyException e) {
            servletRequest.setAttribute("exception", ExceptionCode.INVALID_AUTHORITIES_TOKEN.getCode());
        } catch (Exception e) {
            servletRequest.setAttribute("exception", ExceptionCode.UNKNOWN_ERROR.getCode());
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    // Request Header에서 토큰 정보 꺼내오기
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
