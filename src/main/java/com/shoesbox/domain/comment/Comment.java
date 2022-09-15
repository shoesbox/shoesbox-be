package com.shoesbox.domain.comment;

import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.post.Post;
import com.shoesbox.global.common.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "comment")
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nickname;

    @NotBlank
    @Column(nullable = false)
    private String content;

    @NotBlank
    @Column(nullable = false)
    private String profileImageUrl;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    // 작성자 PK (읽기전용으로만 사용할 것)
    @Column(name = "member_id", updatable = false, insertable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "post_id", updatable = false, insertable = false)
    private Long postId;

    @Builder
    private Comment(String nickname, String content, Member member, Post post, String profileImageUrl) {
        this.nickname = nickname;
        this.content = content;
        this.member = member;
        this.post = post;
        this.profileImageUrl = profileImageUrl;
    }

    public void update(CommentRequestDto commentRequestDto) {
        this.content = commentRequestDto.getContent();
    }
}
