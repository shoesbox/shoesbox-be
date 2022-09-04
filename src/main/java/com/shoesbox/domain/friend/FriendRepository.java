package com.shoesbox.domain.friend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findAllByToMemberIdAndFriendState(long toMemberId, boolean isFriend);

//    Optional<Friend> findByMemberId(long memberId);

    boolean existsByFromMemberIdAndToMemberId(long fromMember, long toMember);
}
