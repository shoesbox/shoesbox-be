package com.shoesbox.domain.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // List<Comment> findAllByPostId(Long postId);

    Optional<Comment> findById(Long commentId);
}