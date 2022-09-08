package com.shoesbox.domain.auth;

import com.shoesbox.global.common.BaseTimeEntity;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Jacksonized
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "refresh_token")
@Entity
public class RefreshToken extends BaseTimeEntity {
    @Id
    @Column
    @NotNull
    // memberId(Member Entity의 PK)가 들어감
    Long memberId;
    @With
    @Column
    @NotBlank
    String tokenValue;
}
