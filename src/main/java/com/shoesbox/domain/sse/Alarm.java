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

    // 알람 확인 상태 (확인시 TRUE, 미확인시 FALSE)
    @Column(nullable = false)
    private boolean isChecked;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column(nullable = false)
    private String content;

    // 발송자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "send_member_id", nullable = false)
    private Member sendMember;

    // 수신자
    @Column(nullable = false)
    private long receiveMemberId;

    @Builder
    private Alarm(Member sendMember, long receiveMemberId, String content, MessageType messageType) {
        this.sendMember = sendMember;
        this.receiveMemberId = receiveMemberId;
        this.content = content;
        this.messageType = messageType;
    }
}
