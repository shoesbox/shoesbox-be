package com.shoesbox.domain.post;

import com.shoesbox.domain.comment.Comment;
import com.shoesbox.domain.member.Member;
import com.shoesbox.global.common.BaseTimeEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;


@Getter
@Entity
@Table(name = "post")
public class Post extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Setter
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private final int createdYear;

    @Column(nullable = false)
    private final int createdMonth;

    @Column(nullable = false)
    private final int createdDay;

    // @Column
    // private List<MultipartFile> images;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    // 작성자 PK (읽기전용으로만 사용할 것)
    @Column(name = "member_id", updatable = false, insertable = false)
    private Long memberId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    protected Post() {
        this.createdYear = LocalDate.now().getYear();
        this.createdMonth = LocalDate.now().getMonthValue();
        this.createdDay = LocalDate.now().getDayOfMonth();
    }

    @Builder
    private Post(long id, String title, String content, String author, Member member) {
        this();
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.member = member;
    }

    protected void update(String title, String content) {
        this.title = title;
        this.content = content;
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

