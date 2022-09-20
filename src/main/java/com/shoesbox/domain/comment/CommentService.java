package com.shoesbox.domain.comment;

import com.shoesbox.domain.friend.FriendService;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.member.MemberRepository;
import com.shoesbox.domain.post.Post;
import com.shoesbox.domain.post.PostRepository;
import com.shoesbox.global.exception.runtime.PostNotFoundException;
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
    private final FriendService friendService;

    @Transactional(readOnly = true)
    public List<CommentResponseDto> readComment(Long postId, long currentMemberId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(
                        () -> new PostNotFoundException("해당 게시글이 존재하지 않습니다."));

        long authorId = post.getMemberId();
        if (authorId != currentMemberId) {
            if (!friendService.isFriend(authorId, currentMemberId)) {
                throw new IllegalArgumentException("해당 게시물에 접근할 수 없습니다.");
            }
        }

        return post.getComments().stream().map(CommentService::toCommentResponseDto).collect(Collectors.toList());
    }

    @Transactional
    public CommentResponseDto createComment(
            String currentMemberNickname,
            String content,
            long currentMemberId,
            long postId) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("해당 게시물을 찾을 수 없습니다."));

        long authorId = post.getMemberId();
        if (authorId != currentMemberId) {
            if (!friendService.isFriend(authorId, currentMemberId)) {
                throw new IllegalArgumentException("해당 게시물에 접근할 수 없습니다.");
            }
        }

        Member currentMember = memberRepository.findById(currentMemberId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Comment comment = Comment.builder()
                .nickname(currentMemberNickname)
                .content(content)
                .member(currentMember)
                .post(post)
                .profileImageUrl(currentMember.getProfileImageUrl())
                .build();
        commentRepository.save(comment);

        return toCommentResponseDto(comment);
    }

    @Transactional
    public CommentResponseDto updateComment(long currentMemberId, long commentId, CommentRequestDto commentRequestDto) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        if (comment.getMemberId() != currentMemberId) {
            throw new UnAuthorizedException("본인이 작성한 댓글만 수정 가능합니다.");
        }

        comment.update(commentRequestDto);

        return toCommentResponseDto(comment);
    }

    @Transactional
    public String deleteComment(long currentMemberId, long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        if (comment.getMemberId() != currentMemberId) {
            throw new UnAuthorizedException("본인이 작성한 댓글만 삭제 가능합니다.");
        }

        commentRepository.delete(comment);

        return "commentId: " + commentId + "삭제 성공";
    }

    public static CommentResponseDto toCommentResponseDto(Comment comment) {
        return CommentResponseDto.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .profileImageUrl(comment.getProfileImageUrl())
                .nickname(comment.getNickname())
                .memberId(comment.getMember().getId())
                .postId(comment.getPost().getId())
                .createdAt(comment.getCreatedAt())
                .modifiedAt(comment.getModifiedAt())
                .build();
    }
}
