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
@Table(name = "alarms")
public class Alarm extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column(nullable = false)
    private String content;

    // 발송자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_member_id", nullable = false)
    private Member senderMember;

    // 수신자
    @Column(nullable = false)
    private long receiverMemberId;

    @Builder
    private Alarm(Member senderMember, long receiverMemberId, String content, MessageType messageType) {
        this.senderMember = senderMember;
        this.receiverMemberId = receiverMemberId;
        this.content = content;
        this.messageType = messageType;
    }
}
