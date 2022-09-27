package com.shoesbox.sse;


import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
@Controller
public class SseController {

    // 사용자 식별 - member의 pk값을 파싱
    public static Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private static final long SSE_SESSION_TIMEOUT = 30 * 60 * 1000L;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @RequestMapping(value = "/api/sub", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter subscribe(
            @RequestParam(value = "id", defaultValue = "0", required = false) long memberId,
            HttpServletResponse response) {

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        // memberId 검증
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalArgumentException("Member Not Found Error in Notification Controller")
        );
//        if (memberId == SecurityUtil.getCurrentMemberId()) {
        // 현재 클라이언트를 위한 SseEmitter 생성
        SseEmitter sseEmitter = new SseEmitter(SSE_SESSION_TIMEOUT);
        ExecutorService service = Executors.newSingleThreadExecutor();

        service.execute(() -> {
            try {
                // 연결
                sseEmitter.send(SseEmitter.event().name("connect").data(memberId + "has connected\n\n"));
            } catch (IOException e) {
                e.printStackTrace();
                sseEmitter.completeWithError(e);
                return;
            }
//            sseEmitter.complete();
        });
        // user의 memberId를 key값으로 해서 SseEmitter를 저장
        sseEmitters.put(memberId, sseEmitter);
        sseEmitter.onCompletion(() -> sseEmitters.remove(memberId));
        sseEmitter.onTimeout(() -> sseEmitters.remove(memberId));
        sseEmitter.onError((e) -> sseEmitters.remove(memberId));

        return sseEmitter;
//       f
    }

    @RequestMapping(value = "/api/sub/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void TestSSE(@RequestParam(value = "data", defaultValue = "hi", required = false) String data, HttpServletResponse response) {

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        for (SseEmitter emitter : sseEmitters.values()) {
            try {
                emitter.send(SseEmitter.event().name("test").data(data + "/n"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 댓글 알림 서비스 테스트
        notificationService.notifyAddCommentEvent(1);


    }

}
