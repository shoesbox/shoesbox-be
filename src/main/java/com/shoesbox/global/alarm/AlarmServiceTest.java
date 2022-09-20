//package com.shoesbox.global.alarm;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.web.server.LocalServerPort;
//import org.springframework.messaging.simp.stomp.StompFrameHandler;
//import org.springframework.messaging.simp.stomp.StompHeaders;
//import org.springframework.messaging.simp.stomp.StompSession;
//import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
//import org.springframework.web.socket.WebSocketHttpHeaders;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//import org.springframework.web.socket.messaging.WebSocketStompClient;
//import org.springframework.web.socket.sockjs.client.SockJsClient;
//import org.springframework.web.socket.sockjs.client.WebSocketTransport;
//
//import java.util.Arrays;
//import java.util.Map;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.LinkedBlockingDeque;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Transactional
//class AlarmServiceTest {
//
//    static final String WEBSOCKET_TOPIC = "/sub/";
//
//    BlockingQueue<String> blockingQueue;
//    WebSocketStompClient stompClient;
//
//    @Autowired
//    RegionRepository regionRepository;
//    @Autowired TownRepository townRepository;
//    @Autowired SignService signService;
//    @Autowired MessageService messageService;
//    @LocalServerPort
//    Integer port;
//
//    @BeforeEach
//    public void beforeEach() {
//        ...
//        // 발신자와 수신자 회원가입
//        signService.registerUser(new UserRegisterRequestDto("sender", "1234", "sender"));
//        signService.registerUser(new UserRegisterRequestDto("receiver", "1234", "receiver"));
//        blockingQueue = new LinkedBlockingDeque<>();
//        stompClient = new WebSocketStompClient(new SockJsClient(
//                Arrays.asList(new WebSocketTransport(new StandardWebSocketClient()))));
//    }
//
//    @Test
//    public void connectionFailedByInvalidateTokenTest() { // 유효하지않은 토큰 연결 테스트
//
//        // given
//        StompHeaders headers = new StompHeaders(); // 헤더에 토큰 값 삽입
//        headers.add("token", "invalidate token");
//
//        // when, then
//        // 잘못된 토큰으로 연결하면 예외 발생
//        Assertions.assertThatThrownBy(() -> {
//            stompClient
//                    .connect(getWsPath(), new WebSocketHttpHeaders() ,headers, new StompSessionHandlerAdapter() {})
//                    .get(10, SECONDS);
//        }).isInstanceOf(ExecutionException.class);
//    }
//
//    @Test
//    public void alarmByMessageTest() throws Exception { // 메시지 수신 시 알람 테스트
//
//        // given
//        UserLoginResponseDto sender = signService.loginUser(new UserLoginRequestDto("sender", "1234"));
//        UserLoginResponseDto receiver = signService.loginUser(new UserLoginRequestDto("receiver", "1234"));
//        StompHeaders headers = new StompHeaders(); // 헤더에 토큰 삽입
//        headers.add("token", sender.getToken());
//        StompSession session = stompClient
//                .connect(getWsPath(), new WebSocketHttpHeaders() ,headers, new StompSessionHandlerAdapter() {})
//                .get(10, SECONDS); // 연결
//        session.subscribe(WEBSOCKET_TOPIC + receiver.getId(), new DefaultStompFrameHandler()); // "/sub/{userId}" 구독
//
//        // when
//        MessageCreateRequestDto requestDto = new MessageCreateRequestDto(sender.getId(), receiver.getId(), "MESSAGE TEST");
//        MessageDto messageDto = messageService.createMessage(requestDto); // 메세지 전송
//
//        // then
//        ObjectMapper mapper = new ObjectMapper();
//        String jsonResult = blockingQueue.poll(10, SECONDS); // 소켓 수신 내역 꺼내옴
//        Map<String, String> result = mapper.readValue(jsonResult, Map.class); // json 파싱
//        assertThat(result.get("message")).isEqualTo(messageDto.getMessage());
//    }
//
//    class DefaultStompFrameHandler implements StompFrameHandler {
//        @Override
//        public Type getPayloadType(StompHeaders stompHeaders) {
//            return byte[].class;
//        }
//
//        @Override
//        public void handleFrame(StompHeaders stompHeaders, Object o) {
//            blockingQueue.offer(new String((byte[]) o));
//        }
//    }
//
//    private String getWsPath() {
//        return String.format("ws://localhost:%d/ws-stomp", port);
//    }
//
//    public MessageDto createMessage(MessageCreateRequestDto requestDto) {
//        // 메시지 내용
//        alarmService.alarmByMessage(messageDto);
//        return messageDto;
//    }
//
//}
