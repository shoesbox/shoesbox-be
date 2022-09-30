package com.shoesbox.domain.sse;


import com.shoesbox.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Controller
@Slf4j
public class SseController {
    // 사용자 식별 - member의 pk값을 파싱
    public static final Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private static final long SSE_SESSION_TIMEOUT = 3600 * 1000L;
    //    private static final int coreCount = Runtime.getRuntime().availableProcessors();
//    public static final ExecutorService sseExcutor = Executors.newFixedThreadPool(coreCount);
//    public static final ExecutorService sseExecutor = Executors.newSingleThreadExecutor();

    @GetMapping(value = "/api/sub", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter subscribe(HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        // memberId 검증
        long currentMemberId = SecurityUtil.getCurrentMemberId();
        // 현재 클라이언트를 위한 SseEmitter 생성
        SseEmitter sseEmitter = new SseEmitter(SSE_SESSION_TIMEOUT);
        try {
            // 연결 -
            sseEmitter.send(SseEmitter.event().name("connect").data(currentMemberId + "has connected\n\n"));
            log.info(">>>>>>>>>>>>>>>>> [Connection Established] memberId : " + currentMemberId + " has connected.");
            Thread.sleep(100);
            log.info("awake!");
//                sseEmitter.complete();
        } catch (IOException | InterruptedException e) {
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
            sseEmitter.completeWithError(e);
        }

//        sseExecutor.execute(() -> {
//            try {
//                // 연결 -
//                sseEmitter.send(SseEmitter.event().name("connect").data(currentMemberId + "has connected\n\n"));
//                log.info(">>>>>>>>>>>>>>>>> [Connection Established] memberId : " + currentMemberId + " has connected.");
//                Thread.sleep(100);
//                log.info("awake!");
////                sseEmitter.complete();
//            } catch (IOException | InterruptedException e) {
//                log.error(e.getLocalizedMessage());
//                e.printStackTrace();
//                sseEmitter.completeWithError(e);
//            }
//        });

        log.info("구독 성공!");

        // 로그인한 유저가 새로고침 한 경우
        if (sseEmitters.containsKey(currentMemberId)) {
            log.error("이미 로그인한 사용자: " + currentMemberId + "입니다.");
            return sseEmitters.get(currentMemberId);
        }
        // user의 memberId를 key값으로 해서 SseEmitter를 저장
        sseEmitters.put(currentMemberId, sseEmitter);
        sseEmitter.onCompletion(
                () -> sseEmitters.remove(currentMemberId));
        sseEmitter.onTimeout(
                () -> sseEmitters.remove(currentMemberId));
        sseEmitter.onError(
                (e) -> sseEmitters.remove(currentMemberId));
        return sseEmitter;
    }

    @GetMapping(value = "/api/sub/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void TestSSE(
            @RequestParam(value = "data", defaultValue = "hi", required = false) String data,
            HttpServletResponse response) {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        for (SseEmitter emitter : sseEmitters.values()) {
            try {
                // STRING 형식으로 데이터 전송하기
                emitter.send(SseEmitter.event().name("message").data(data + "\n\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @GetMapping(value = "/api/sub/adminAlarm", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void adminAlarm(
            @RequestParam(value = "data", defaultValue = "hi", required = false) String data,
            HttpServletResponse response) {

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        // todo : 관리자 검증 (테스트 계정?)
        String message = String.format("[관리자] 알립니다.\n%s", data);
        for (SseEmitter emitter : sseEmitters.values()) {
            try {
                // STRING 형식으로 데이터 전송하기
                emitter.send(SseEmitter.event().name("message").data(message + "\n\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
