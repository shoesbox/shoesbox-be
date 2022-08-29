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
}
