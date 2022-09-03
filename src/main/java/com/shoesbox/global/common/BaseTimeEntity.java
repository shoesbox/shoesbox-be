package com.shoesbox.global.common;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public abstract class BaseTimeEntity {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private String createdAt;
    @LastModifiedDate
    @Column(nullable = false)
    private String modifiedAt;

    // 날짜 포맷 변경
    // 엔티티 insert 이전에 실행
    @PrePersist
    public void onPrePersist() {
        this.createdAt =
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm:ss" +
                                ".SS"));
        this.modifiedAt = this.createdAt;
    }

    // 엔티티 update 이전에 실행
    @PreUpdate
    public void onPreUpdate() {
        this.modifiedAt = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm:ss.SS"));
    }
}

