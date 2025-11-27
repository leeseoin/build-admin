package com.BO.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Refresh Token 엔티티
 * 테이블: tb_refresh_token
 */
@Entity
@Table(name = "tb_refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token", nullable = false, length = 500, unique = true)
    private String token;

    @Column(name = "userId")
    private Long userId;

    @Column(name = "adminId")
    private Integer adminId;

    @Enumerated(EnumType.STRING)
    @Column(name = "userType", nullable = false)
    private UserType userType;

    @Column(name = "expiresAt", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "lastUsedAt")
    private LocalDateTime lastUsedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 토큰 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 일반 사용자 토큰 여부 확인
     */
    public boolean isUserToken() {
        return UserType.USER.equals(userType);
    }

    /**
     * 관리자 토큰 여부 확인
     */
    public boolean isAdminToken() {
        return UserType.ADMIN.equals(userType);
    }

    /**
     * 토큰 사용 시간 업데이트
     */
    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
