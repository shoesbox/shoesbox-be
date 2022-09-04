package com.shoesbox.domain.friend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findAllByFriendIdAndFriendState(long memberId, FriendState state);

    boolean existsByFriendIdAndAndMemberId(long friendId, long memberId);
}
