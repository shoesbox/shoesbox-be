package com.shoesbox.domain.friend;

import com.shoesbox.domain.member.Member;
import com.shoesbox.global.common.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "friend")
public class Friend extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 요청 상태 (요청시 REQUEST, 수락했을 시 FRIEND)
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FriendState friendState;

    // 친구 요청한 member
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_member_id", nullable = false)
    private Member fromMember;

    // 친구 요청받는 member
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_member_id", nullable = false)
    private Member toMember;

    @Builder
    private Friend(Member fromMember, Member toMember, FriendState friendState) {
        this.fromMember = fromMember;
        this.toMember = toMember;
        this.friendState = friendState;
    }

    public void updateFriendState(FriendState friendState) {
        this.friendState = friendState;
    }
}
