package com.shoesbox.domain.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shoesbox.domain.comment.Comment;
import com.shoesbox.domain.friend.Friend;
import com.shoesbox.domain.photo.Photo;
import com.shoesbox.domain.post.Post;
import com.shoesbox.domain.sse.Alarm;
import com.shoesbox.global.common.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;
    @NotBlank
    @Email
    @Column(unique = true)
    private String email;
    @Column
    @NotBlank
    // @Size(min = 4, max = 20)
    private String nickname;
    @Column
    @NotBlank
    @JsonIgnore
    private String password;
    @Column
    @NotBlank
    private String profileImageUrl;
    @Column
    @NotBlank
    @JsonIgnore
    private final String authority = "ROLE_USER";

    // 작성글 리스트
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member",
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Post> posts = new ArrayList<>();
    private int postCount;

    // 댓글 리스트
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member",
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();
    private int commentCount;

    // 친구들
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "fromMember",
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Friend> fromMembers = new ArrayList<>();
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "toMember",
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Friend> toMembers = new ArrayList<>();
    private int friendCount;

    // 사진
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member",
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Photo> photos = new ArrayList<>();

    // 알람
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sendMember",
            cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Alarm> alarms = new ArrayList<>();

    public void updateInfo(String nickname, String profileImageUrl) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }
}
