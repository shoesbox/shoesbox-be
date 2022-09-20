package com.shoesbox.global.alarm;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AlarmController {

    private final SimpMessageSendingOperations messagingTemplate; // @EnableWebSocketMessageBroker를 통해 등록되는 Bean, Broker로 메시지 전달
    private final AlarmService alarmService;

    // stomp 테스트 화면
    @GetMapping("/alarm/stomp")
    public String stompAlarm() {
        return "/index";
    }

    @MessageMapping("/{userId}") // 클라이언트가 전송하는 경로
    // WebSocketConfig에 등록한 applicatonDestinationPrfixes와 @MessageMapping의 경로가 합쳐짐.
    public void message(@DestinationVariable("userId") Long userId, @RequestParam MessageDto messageDto) { // messageDto를 어디에 붙일 것인가
        alarmService.alarmByMessage(messageDto);
        messagingTemplate.convertAndSend("/sub/" + userId, "alarm socket connection completed.");
    }
}
