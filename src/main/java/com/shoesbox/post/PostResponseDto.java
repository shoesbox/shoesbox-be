package com.shoesbox.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponseDto {
    private Long post_id;
    private String title;
    private String content;
    private int year;
    private int month;
    private int day;
    private String images;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;


    @Builder
    public PostResponseDto(Post post) {
        this.post_id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.year = post.getCreatedYear();
        this.month = post.getCreatedMonth();
        this.day = post.getCreatedDay();
        this.images = post.getImages();
        this.modifiedAt = post.getModifiedAt();
        this.createdAt = post.getCreatedAt();
    }
}
