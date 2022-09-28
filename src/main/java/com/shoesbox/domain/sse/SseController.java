package com.shoesbox.domain.sse;


import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
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
@Slf4j
public class SseController {

    // 사용자 식별 - member의 pk값을 파싱
    public static Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private static final Long SSE_SESSION_TIMEOUT = 3600 * 1000L;
    private final MemberRepository memberRepository;

    //    private final EmitterRepository emitterRepository;
    public static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();


    @CrossOrigin
    @RequestMapping(value = "/api/sub", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseBodyEmitter subscribe(
            @RequestParam(value = "id", defaultValue = "0", required = false) long memberId,
            @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId,
            HttpServletResponse response) {

//        //1
//        String id = String.valueOf(memberId + '_' + System.currentTimeMillis());
//
//        //2
//        SseEmitter emitter = emitterRepository.save(id, new SseEmitter(SSE_SESSION_TIMEOUT));
//
//        //3
//        sendToClient(emitter, id, "EventStream Created. [userId=" + memberId + "]");
//
//        // 4
//        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
//        if (!lastEventId.isEmpty()) {
//            Map<String, Object> events = emitterRepository.findAllEventCacheStartWithId(String.valueOf(userId));
//            events.entrySet().stream()
//                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
//                    .forEach(entry -> sendToClient(emitter, entry.getKey(), entry.getValue()));
//        }

        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");

        // memberId 검증
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalArgumentException("Member Not Found Error in Notification Controller")
        );
        // 현재 클라이언트를 위한 SseEmitter 생성
        SseEmitter sseEmitter = new SseEmitter(SSE_SESSION_TIMEOUT);
        ExecutorService service = Executors.newSingleThreadExecutor();

        // 멤버 검증
        //        long myMemberId = SecurityUtil.getCurrentMemberId();
//        if (myMemberId != memberId) {
//            try {
//                // 503 error를 방지하기 위한 더미 데이터
//                sseEmitter.send(SseEmitter.event().name("error").data("멤버 아이디가 일치하지 않습니다.\n\n"));
//            } catch (IOException e) {
//                sseEmitter.completeWithError(e);
//                throw new IllegalArgumentException(e);
//            }
//            return sseEmitter;
//        }

        service.execute(() -> {
            try {
                // 연결 -
                sseEmitter.send(SseEmitter.event().name("connect").data(memberId + "has connected\n\n"));
                log.info(">>>>>>>>>>>>>>>>> [Connection Established] memberId : " + memberId + " has connected.");
            } catch (IOException e) {
                e.printStackTrace();
                sseEmitter.completeWithError(e);
            }
        });

        // 로그인한 유저가 새롭게 로그인한 경우 업데이트
        if (sseEmitters.containsKey(memberId)) {
            return sseEmitters.get(memberId);
        }
        // user의 memberId를 key값으로 해서 SseEmitter를 저장
        sseEmitters.put(memberId, sseEmitter);
        sseEmitter.onCompletion(() -> sseEmitters.remove(memberId));
        sseEmitter.onTimeout(() -> sseEmitters.remove(memberId));
        sseEmitter.onError((e) -> sseEmitters.remove(memberId));
//        sseEmitter.complete() -> 반드시 return값을 void로 처리할 것 complete 후 함수가 끝남;
        return sseEmitter;
    }

    @RequestMapping(value = "/api/sub/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void TestSSE(@RequestParam(value = "data", defaultValue = "hi", required = false) String data, HttpServletResponse response) {

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
        // 댓글 알림 서비스 테스트
    }

    @RequestMapping(value = "/api/sub/test2")
    public void TestSSE() {

        MessageDto a = MessageDto.builder().senderNickName("sponGbob").postId(1).month(9).day(1).msgType("post").build();

        for (SseEmitter emitter : sseEmitters.values()) {
            try {
                // JSON 형식으로 전송하기
                emitter.send(SseEmitter.event().name("message").data(a.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    private void sendToClient(SseEmitter emitter, String id, Object data) {
//        try {
//            emitter.send(SseEmitter.event()
//                    .id(id)
//                    .name("sse")
//                    .data(data));
//        } catch (IOException exception) {
//            emitterRepository.deleteById(id);
//            throw new RuntimeException("연결 오류!");
//        }
}
