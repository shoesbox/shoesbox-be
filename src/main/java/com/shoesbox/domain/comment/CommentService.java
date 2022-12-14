package com.shoesbox.domain.comment;

import com.shoesbox.domain.comment.dto.CommentRequestDto;
import com.shoesbox.domain.comment.dto.CommentResponseDto;
import com.shoesbox.domain.friend.FriendRepository;
import com.shoesbox.domain.friend.FriendState;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.domain.post.Post;
import com.shoesbox.domain.post.PostRepository;
import com.shoesbox.domain.sse.Alarm;
import com.shoesbox.domain.sse.AlarmRepository;
import com.shoesbox.domain.sse.MessageType;
import com.shoesbox.global.exception.runtime.EntityNotFoundException;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final AlarmRepository alarmRepository;

    @Transactional
    public CommentResponseDto createComment(String content, long currentMemberId, long postId) {
        Post post = getPost(postId);
        checkAuthorization(currentMemberId, post.getMemberId());
        Member currentMember = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new EntityNotFoundException(
                        Member.class.getPackageName()));

        Comment comment = Comment.builder()
                .content(content)
                .member(currentMember)
                .post(post)
                .build();
        commentRepository.save(comment);

        notifyAddCommentEvent(post, currentMemberId, currentMember.getNickname());
        return toCommentResponseDto(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> readComments(long postId, long currentMemberId) {
        Post post = getPost(postId);
        checkAuthorization(currentMemberId, post.getMemberId());
        return post.getComments().stream().map(this::toCommentResponseDto).collect(Collectors.toList());
    }

    @Transactional
    public CommentResponseDto updateComment(long currentMemberId, long commentId, CommentRequestDto commentRequestDto) {
        Comment comment = getComment(commentId);
        checkSelfAuthorization(currentMemberId, comment.getMemberId());
        comment.update(commentRequestDto.getContent());
        return toCommentResponseDto(comment);
    }

    @Transactional
    public String deleteComment(long currentMemberId, long commentId) {
        Comment comment = getComment(commentId);
        checkSelfAuthorization(currentMemberId, comment.getMemberId());
        commentRepository.delete(comment);
        return "commentId: " + commentId + "?????? ??????";
    }

    private Comment getComment(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class.getPackageName()));
    }

    private Post getPost(long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(Post.class.getPackageName()));
    }

    private CommentResponseDto toCommentResponseDto(Comment comment) {
        return CommentResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .profileImageUrl(comment.getMember().getProfileImageUrl())
                .nickname(comment.getMember().getNickname())
                .memberId(comment.getMember().getId())
                .postId(comment.getPost().getId())
                .createdAt(comment.getCreatedAt())
                .modifiedAt(comment.getModifiedAt())
                .build();
    }

    private void checkAuthorization(long currentMemberId, long targetId) {
        if (currentMemberId != targetId && !isFriend(currentMemberId, targetId)) {
            throw new UnAuthorizedException("?????? ????????? ????????????.");
        }
    }

    private void checkSelfAuthorization(long currentMemberId, long targetId) {
        if (currentMemberId != targetId) {
            throw new UnAuthorizedException("?????? ????????? ????????????.");
        }
    }

    private boolean isFriend(long currentMemberId, long targetId) {
        return currentMemberId == targetId
                || friendRepository.existsByFromMemberIdAndToMemberIdAndFriendState(targetId, currentMemberId,
                FriendState.FRIEND)
                || friendRepository.existsByFromMemberIdAndToMemberIdAndFriendState(currentMemberId, targetId,
                FriendState.FRIEND);
    }

    public void notifyAddCommentEvent(Post post, long senderMemberId, String senderNickName) {

        long postId = post.getId();
        long receiverMemberId = post.getMemberId();
        // ????????? ????????? ?????? ?????? ?????? (?????? ?????????)
        int month = post.getDate().getMonthValue();
        int day = post.getDate().getDayOfMonth();

//        // ????????? ??? ??????????????? ?????? ??????
//        if (sseEmitters.containsKey(receiverMemberId) && senderMemberId != receiverMemberId) {
//            SseEmitter sseEmitter = sseEmitters.get(receiverMemberId);
//            MessageDto messageDto = MessageDto.builder()
//                    .postId(postId)
//                    .senderNickName(senderNickName)
//                    .month(month)
//                    .day(day)
//                    .msgType("Comment")
//                    .build();
////            sseExecutor.execute(() -> {
////                try {
////                    sseEmitter.send(SseEmitter.event().name("addComment").data(messageDto, MediaType.APPLICATION_JSON));
////                    Thread.sleep(100);
////                } catch (IOException | InterruptedException e) {
////                    log.error(e.getLocalizedMessage());
////                    sseEmitter.completeWithError(e);
////                }
////            });
//            try {
//                sseEmitter.send(SseEmitter.event().name("addComment").data(messageDto, MediaType.APPLICATION_JSON));
//                Thread.sleep(100);
//            } catch (IOException | InterruptedException e) {
//                log.error(e.getLocalizedMessage());
//                sseEmitter.completeWithError(e);
//            }
//        }

        // ?????? ?????? db??? ??????
        if (senderMemberId != receiverMemberId) {
            saveAlarm(senderMemberId, receiverMemberId, postId, month, day);
        }
    }

    @Transactional
    public void saveAlarm(long senderMemberId, long receiverMemberId, long contentId, int month, int day) {
        String content = contentId + "," + month + "," + day;

        // send: ???????????????, receive: ????????????
        Member senderMember = Member.builder()
                .id(senderMemberId)
                .build();

        Alarm alarm = Alarm.builder()
                .senderMember(senderMember)
                .receiverMemberId(receiverMemberId)
                .content(content)
                .messageType(MessageType.COMMENT)
                .build();

        alarmRepository.save(alarm);
    }
}
