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
    private boolean is_private;
    private int year;
    private int month;
    private int day;
    private String images;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;

    @Builder
    public PostResponseDto(Post post){
        this.post_id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.is_private = post.is_private();
        this.year = post.getYear();
        this.month = post.getMonth();
        this.day = post.getDay();
        this.images = post.getImages();
        this.modifiedAt = post.getModifiedAt();

    }
}
