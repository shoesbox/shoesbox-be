package com.shoesbox.sse;

import com.shoesbox.domain.post.Post;
import com.shoesbox.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static com.shoesbox.sse.SseController.sseEmitters;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private final PostRepository postRepository;

    public void notifyAddCommentEvent(long postId) {

        // 댓글에 대한 처리 후 해당 댓글이 달린 게시글의 pk값으로 게시글을 조회
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("[Notification Service] 해당 게시물을 찾을 수 없습니다.")
        );

        long memberId = post.getMemberId();

        if (sseEmitters.containsKey(memberId)) {
            SseEmitter sseEmitter = sseEmitters.get(memberId);
            try {
                sseEmitter.send(SseEmitter.event().name("addComment").data("댓글이 달렸습니다!!!!!\n\n"));
            } catch (Exception e) {
                sseEmitters.remove(memberId);
            }
        } // todo : 접속 중이 아닌 유저의 경우 db에 저장 후 차후 알림

    }
}
