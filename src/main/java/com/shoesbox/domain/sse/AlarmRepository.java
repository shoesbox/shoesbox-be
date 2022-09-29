package com.shoesbox.domain.sse;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findAllByReceiverMemberId(long receiverMemberId);
}
