package com.shoesbox.domain.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shoesbox.domain.comment.Comment;
import com.shoesbox.domain.photo.Photo;
import com.shoesbox.domain.post.Post;
import com.shoesbox.global.common.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "member")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;
    // @Column(unique = true)
    // @NotBlank
    // private String uniqueId;
    @Column(unique = true)
    @NotBlank
    @Email
    private String email;
    @Column
    @NotBlank
    // @Size(min = 4, max = 20)
    private String nickname;
    @Column
    @NotBlank
    // @Size(min = 8, max = 20)
    @JsonIgnore
    private String password;
    @Column
    @NotBlank
    private String profileImageUrl;
    @Column
    @NotBlank
    private String selfDescription;
    @Column
    @NotBlank
    @JsonIgnore
    private final String authority = "ROLE_USER";

    // 작성글 리스트
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    private int postCount;

    // 댓글 리스트
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    private int commentCount;

    // 사진
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos;

    public Member updateInfo(
            String nickname,
            String profileImageUrl,
            String selfDescription) {
        if (nickname != null) {
            this.nickname = nickname;
        }

        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }

        if (selfDescription != null) {
            this.selfDescription = selfDescription;
        }

        return this;
    }

    public int addPostCount() {
        return ++postCount;
    }

    public int minusPostCount() {
        return --postCount;
    }

    public int addCommentCount() {
        return ++commentCount;
    }

    public int minusCommentCount() {
        return --commentCount;
    }
}
