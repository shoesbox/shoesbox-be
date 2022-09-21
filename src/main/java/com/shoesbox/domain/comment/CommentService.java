package com.shoesbox.domain.comment;

import com.shoesbox.domain.comment.dto.CommentRequestDto;
import com.shoesbox.domain.comment.dto.CommentResponseDto;
import com.shoesbox.domain.friend.FriendRepository;
import com.shoesbox.domain.friend.FriendState;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.domain.post.Post;
import com.shoesbox.domain.post.PostRepository;
import com.shoesbox.global.exception.runtime.EntityNotFoundException;
import com.shoesbox.global.exception.runtime.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;

    @Transactional
    public CommentResponseDto createComment(String content, long currentMemberId, long postId) {
        Post post = getPost(postId);
        checkSelfAuthorization(currentMemberId, post.getMemberId());
        Member currentMember = memberRepository.findById(currentMemberId)
                                               .orElseThrow(() -> new EntityNotFoundException(
                                                       Member.class.getPackageName()));
        Comment comment = Comment.builder()
                                 .content(content)
                                 .member(currentMember)
                                 .post(post)
                                 .build();
        commentRepository.save(comment);
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
        return "commentId: " + commentId + "삭제 성공";
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
        checkSelfAuthorization(currentMemberId, targetId);
        if (!isFriend(currentMemberId, targetId)) {
            throw new UnAuthorizedException("접근 권한이 없습니다.");
        }
    }

    private void checkSelfAuthorization(long currentMemberId, long targetId) {
        if (currentMemberId != targetId) {
            throw new UnAuthorizedException("접근 권한이 없습니다.");
        }
    }

    private boolean isFriend(long currentMemberId, long targetId) {
        return currentMemberId == targetId
                || friendRepository.existsByFromMemberIdAndToMemberIdAndFriendState(targetId, currentMemberId,
                                                                                    FriendState.FRIEND)
                || friendRepository.existsByFromMemberIdAndToMemberIdAndFriendState(currentMemberId, targetId,
                                                                                    FriendState.FRIEND);
    }
}
