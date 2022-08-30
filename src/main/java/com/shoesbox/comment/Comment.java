package com.shoesbox.comment;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Entity(name="comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long postId;

    public Comment(Long postId, CommentRequestDto commentRequestDto){
        this.username = commentRequestDto.getUsername();
        this.content = commentRequestDto.getContent();
        this.userId = commentRequestDto.getUserId();
        this.postId = postId;
    }

    public void update(CommentRequestDto commentRequestDto){
        this.username = commentRequestDto.getUsername();
        this.content = commentRequestDto.getContent();
    }
}
