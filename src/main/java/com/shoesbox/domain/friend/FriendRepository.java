package com.shoesbox.domain.friend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findAllByToMemberIdAndFriendState(long toMemberId, boolean friendState);

    Optional<Friend> findByFromMemberIdAndFriendState(long fromMemberId, boolean friendState);

    boolean existsByFromMemberIdAndToMemberId(long fromMember, long toMember);
}
