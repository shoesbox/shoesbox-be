package com.shoesbox.post;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "posts")
public class Post extends Timestamped {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
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


    @Builder
    public Post(String title, String content, String images) {
        this.title = title;
        this.content = content;
        this.images = images;
    }

    public void update(String title, String content, String images) {
        this.title = title;
        this.content = content;
        this.images = images;
    }


//    @Column
//    private List<Photo> photo = new ArrayList<>();
//
//    public void addPhoto(Photo photo) {
//        this.photo.add(photo);
//
//        if (photo.getPost() != this) {
//            photo.setPost(this);
//        }
//    }


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

