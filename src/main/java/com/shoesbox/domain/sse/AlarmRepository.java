package com.shoesbox.domain.sse;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findAllByReceiveMemberId(long receiveMemberId);

    Optional<Alarm> findByReceiveMemberId(long receiveMemberId);
}
