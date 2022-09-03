package com.shoesbox.global.security.jwt;


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
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String exception = (String) request.getAttribute("exception");

        if (exception == null) {
            setResponse(response, ExceptionCode.UNKNOWN_ERROR);
        }
        // 잘못된 타입의 토큰인 경우
        else if (exception.equals(ExceptionCode.INVALID_SIGNATURE_TOKEN.getCode())) {
            setResponse(response, ExceptionCode.INVALID_SIGNATURE_TOKEN);
        }
        // 토큰 만료된 경우
        else if (exception.equals(ExceptionCode.EXPIRED_TOKEN.getCode())) {
            setResponse(response, ExceptionCode.EXPIRED_TOKEN);
        }
        // 지원되지 않는 토큰인 경우
        else if (exception.equals(ExceptionCode.UNSUPPORTED_TOKEN.getCode())) {
            setResponse(response, ExceptionCode.UNSUPPORTED_TOKEN);
        }
        // 토큰이 없거나 이상한 값이 들어온 경우
        else if (exception.equals(ExceptionCode.WRONG_TOKEN.getCode())) {
            setResponse(response, ExceptionCode.WRONG_TOKEN);
        }
        // 토큰에 권한 정보가 없는 경우
        else if (exception.equals(ExceptionCode.INVALID_AUTHORITIES_TOKEN.getCode())) {
            setResponse(response, ExceptionCode.INVALID_AUTHORITIES_TOKEN);
        }
        // 로그아웃한 유저일 경우(db에 userId와 맞는 리프레쉬 토큰이 없는 경우)
        else if (exception.equals(ExceptionCode.LOGGED_OUT_TOKEN.getCode())) {
            setResponse(response, ExceptionCode.LOGGED_OUT_TOKEN);
        } else {
            setResponse(response, ExceptionCode.ACCESS_DENIED);
        }
    }

    // 한글 출력을 위해 getWriter() 사용
    private void setResponse(HttpServletResponse response,
                             ExceptionCode exceptionCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        JSONObject responseJson = new JSONObject();
        responseJson.put("code", exceptionCode.getCode());
        responseJson.put("timestamp", LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        responseJson.put("message", exceptionCode.getMessage());

        response.getWriter().print(responseJson);
    }
}
