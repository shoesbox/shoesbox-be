package com.shoesbox.domain.friend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findAllByToMemberIdAndFriendState(long toMemberId, FriendState friendState);
    List<Friend> findAllByFromMemberIdAndFriendState(long fromMemberId, FriendState friendState);

    Friend findByToMemberIdAndFromMemberId(long toMember, long fromMember);

    Optional<Friend> findByFromMemberIdAndToMemberIdAndFriendState(long fromMemberId, long toMemberId, FriendState friendState);

    boolean existsByFromMemberIdAndToMemberIdAndFriendState(long fromMemberId, long toMemberId, FriendState friendState);
}
