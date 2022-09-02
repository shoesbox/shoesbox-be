package com.shoesbox.post;

import com.shoesbox.comment.Comment;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;


@Getter
@NoArgsConstructor
@Entity
@Table(name = "posts")
public class Post {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Setter
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int createdYear;

    @Column(nullable = false)
    private int createdMonth;

    @Column(nullable = false)
    private int createdDay;

    @Column
    private String images;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post")
    private List<Comment> comments;

    @Builder
    public Post(String title, String content, String images) {
        this.title = title;
        this.content = content;
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

