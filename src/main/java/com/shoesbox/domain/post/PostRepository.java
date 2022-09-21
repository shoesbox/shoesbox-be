package com.shoesbox.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    boolean existsByMemberIdAndDate(long memberId, LocalDate createdDate);

    List<Post> findAllByMemberIdAndDateBetween(Long memberId, LocalDate firstDay, LocalDate lastDay);
}
