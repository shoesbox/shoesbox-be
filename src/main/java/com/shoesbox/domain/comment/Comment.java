package com.shoesbox.domain.comment;

import com.shoesbox.domain.post.Post;
import com.shoesbox.global.common.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@Entity(name="comment")
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

    @Column(nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "post_id", updatable = false, insertable = false)
    private Long postId;

    public Comment(CommentRequestDto commentRequestDto, Post post){
        this.nickname = commentRequestDto.getNickname();
        this.content = commentRequestDto.getContent();
        this.memberId = commentRequestDto.getMemberId();
        this.post = post;
    }

    public void update(CommentRequestDto commentRequestDto){
        this.content = commentRequestDto.getContent();
    }
}
