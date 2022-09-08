package com.shoesbox.domain.photo;

import com.shoesbox.domain.member.Member;
import com.shoesbox.domain.post.Post;
import com.shoesbox.global.common.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "photo")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Photo extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

//    @Column
//    private int state = 1;


    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    // 작성자 PK (읽기전용으로만 사용할 것)
    @Column(name = "member_id", updatable = false, insertable = false)
    private Long memberId;

    // 게시물
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    @Column(name = "post_id", updatable = false, insertable = false)
    private Long postId;


}