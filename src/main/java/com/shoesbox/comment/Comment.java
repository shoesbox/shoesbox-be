package com.shoesbox.comment;

import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Entity(name="comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id")
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

    public String getUsername(){
        return this.username;
    }
    public String getContent(){
        return this.content;
    }

    public void update(CommentRequestDto commentRequestDto){
        this.username = commentRequestDto.getUsername();
        this.content = commentRequestDto.getContent();
    }
}
