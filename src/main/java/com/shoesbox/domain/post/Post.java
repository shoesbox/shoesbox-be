package com.shoesbox.domain.post;

import com.shoesbox.domain.comment.Comment;
import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.photo.Photo;
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
// TODO: 임시로  넣은 것. 제거해야 함!!
@Setter
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
    private String nickname;

    @Column(nullable = false)
    private LocalDate createdDate;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    // 작성자 PK (읽기전용으로만 사용할 것)
    @Column(name = "member_id", updatable = false, insertable = false)
    private Long memberId;

    // 댓글
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    // 이미지
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos;

    protected Post() {
        this.createdDate = LocalDate.now();
    }

    @Builder
    private Post(long id, String title, String content, String nickname, Member member) {
        this();
        this.id = id;
        this.title = title;
        this.content = content;
        this.nickname = nickname;
        this.member = member;
    }

    protected void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
