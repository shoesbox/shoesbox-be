package com.shoesbox.global.alarm;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

//    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() == StompCommand.CONNECT) { // CONNECT인 경우 토큰을 검증
            // todo : 연결 성공 시 필요한 작업 조치

//            if (!jwtTokenProvider.validateToken(accessor.getFirstNativeHeader("token")))
//                throw new AccessDeniedException(""); // 토큰 실패 시 예외 발생
        }
        return message;
    }
}
