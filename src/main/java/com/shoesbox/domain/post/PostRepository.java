package com.shoesbox.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findById(Long postId);

    Page<Post> findByMemberIdAndCreatedYearAndCreatedMonth(Pageable pageable, Long memberId, int year, int month);
}
