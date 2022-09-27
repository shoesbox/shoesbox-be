package com.shoesbox.domain.sse;

import com.shoesbox.domain.member.Member;
import com.shoesbox.global.common.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "alarm")
public class Alarm extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    // 발송자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "send_member_id", nullable = false)
    private Member sendMember;

    // 수신자
    @Column(nullable = false)
    private long receiveMemberId;

    // post 혹은 comment의 Id + "," + month + "," + day 형태로 저장
    @Column(nullable = false)
    private String content;

    @Builder
    private Alarm(Member sendMember, long receiveMemberId, String content, MessageType messageType) {
        this.sendMember = sendMember;
        this.receiveMemberId = receiveMemberId;
        this.content = content;
        this.messageType = messageType;
    }
}
