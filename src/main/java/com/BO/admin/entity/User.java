package com.BO.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 일반 사용자 엔티티
 * 테이블: tb_user
 */
@Entity
@Table(name = "tb_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;

    @Column(name = "loginId", nullable = false)
    private String loginId;

    @Column(name = "userName", nullable = false)
    private String userName;

    @Builder.Default
    @Column(name = "socialLoginYn", nullable = false)
    private Boolean socialLoginYn = false;

    @Column(name = "lastLoginAt")
    private LocalDateTime lastLoginAt;

    @Builder.Default
    @Column(name = "subscribeInService", nullable = false)
    private Boolean subscribeInService = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "registeredPath", nullable = false)
    private RegisteredPath registeredPath = RegisteredPath.EMAIL;

    @Enumerated(EnumType.STRING)
    @Column(name = "withdrawalType")
    private WithdrawalType withdrawalType;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "registeredAt", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "updatedBy")
    private Long updatedBy;

    @Column(name = "updatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deactivatedAt")
    private LocalDateTime deactivatedAt;

    @Column(name = "withdrawalAt")
    private LocalDateTime withdrawalAt;

    @Builder.Default
    @Column(name = "language", length = 10, nullable = false)
    private String language = "ENG";

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 소셜 로그인 사용자 여부 확인
     */
    public boolean isSocialUser() {
        return Boolean.TRUE.equals(socialLoginYn);
    }

    /**
     * 탈퇴한 사용자 여부 확인
     */
    public boolean isWithdrawn() {
        return withdrawalAt != null;
    }

    /**
     * 비활성화된 사용자 여부 확인
     */
    public boolean isDeactivated() {
        return deactivatedAt != null;
    }
}
