package com.shoesbox.domain.sse;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    List<Alarm> findAllBySenderMemberIdAndReceiverMemberId(long senderMemberId, long receiverMemberId);

    Alarm findBySenderMemberId(long senderMemberId);

    List<Alarm> findAllByReceiverMemberId(long receiverMemberId);

    List<Alarm> findAllBySenderMemberIdAndReceiverMemberIdAndMessageType(long senderMemberId, long receiverMemberId, MessageType type);
}
