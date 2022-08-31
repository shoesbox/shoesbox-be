package com.shoesbox.post;

import io.jsonwebtoken.lang.Assert;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity(name = "post")
public class Post extends Timestamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean is_private;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(nullable = false)
    private int day;

    @Column
    private String images;

    @Builder
    public Post(String title, String content, boolean is_private, int year, int month, int day, String images) {
        Assert.hasText(title, "title must not be empty");

        this.title = title;
        this.content = content;
        this.is_private = is_private;
        this.year = year;
        this.month = month;
        this.day = day;
        this.images = images;
    }


//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_user_post"))
//    private User user;}
//    public void mapToUser(User userFoundById) {
//        this.user = userFoundById;
//        userFoundById.mapToContents(this);
//    }
//    public void mapToPost(String images) {
//        this.images = images;
//    }

}

