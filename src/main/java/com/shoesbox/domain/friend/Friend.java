package com.shoesbox.domain.friend;

import com.shoesbox.domain.member.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "friend")
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 요청 상태 (요청시 request, 수락했을 시 accept)
    @Column
    @Enumerated(EnumType.STRING)
    private FriendState friendState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "member_id", updatable = false, insertable = false)
    private Long memberId;

    @Column(name = "friend_id")
    private long friendId;

    @Column(name = "friend_name")
    private String friendName;

    @Builder
    private Friend(Member member, long friendId, String friendName, FriendState friendState){
        this.member = member;
        this.friendId = friendId;
        this.friendName = friendName;
        this.friendState = friendState;
    }
}
