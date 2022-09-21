package com.shoesbox.global.config.jwt;


import org.json.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        String exception = (String) request.getAttribute("exception");

        if (exception == null) {
            setResponse(response, JwtExceptionCode.UNKNOWN_ERROR);
        }
        // 잘못된 타입의 토큰인 경우
        else if (exception.equals(JwtExceptionCode.INVALID_SIGNATURE_TOKEN.getCode())) {
            setResponse(response, JwtExceptionCode.INVALID_SIGNATURE_TOKEN);
        }
        // 토큰 만료된 경우
        else if (exception.equals(JwtExceptionCode.EXPIRED_TOKEN.getCode())) {
            setResponse(response, JwtExceptionCode.EXPIRED_TOKEN);
        }
        // 지원되지 않는 토큰인 경우
        else if (exception.equals(JwtExceptionCode.UNSUPPORTED_TOKEN.getCode())) {
            setResponse(response, JwtExceptionCode.UNSUPPORTED_TOKEN);
        }
        // 토큰이 없거나 이상한 값이 들어온 경우
        else if (exception.equals(JwtExceptionCode.WRONG_TOKEN.getCode())) {
            setResponse(response, JwtExceptionCode.WRONG_TOKEN);
        }
        // 토큰에 권한 정보가 없는 경우
        else if (exception.equals(JwtExceptionCode.INVALID_AUTHORITIES_TOKEN.getCode())) {
            setResponse(response, JwtExceptionCode.INVALID_AUTHORITIES_TOKEN);
        }
        // 로그아웃한 유저일 경우(db에 userId와 맞는 리프레쉬 토큰이 없는 경우)
        else if (exception.equals(JwtExceptionCode.LOGGED_OUT_TOKEN.getCode())) {
            setResponse(response, JwtExceptionCode.LOGGED_OUT_TOKEN);
        } else {
            setResponse(response, JwtExceptionCode.ACCESS_DENIED);
        }
    }

    // 한글 출력을 위해 getWriter() 사용
    private void setResponse(
            HttpServletResponse response,
            JwtExceptionCode jwtExceptionCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        JSONObject responseJson = new JSONObject();
        responseJson.put("code", jwtExceptionCode.getCode());
        responseJson.put("timestamp", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        responseJson.put("message", jwtExceptionCode.getMessage());

        response.getWriter().print(responseJson);
    }
}
