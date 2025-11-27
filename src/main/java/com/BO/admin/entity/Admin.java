package com.BO.admin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 관리자 엔티티
 * 테이블: tb_adminAccount
 */
@Entity
@Table(name = "tb_adminAccount")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adminId")
    private Integer adminId;

    @Column(name = "loginId", nullable = false)
    private String loginId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "memo")
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 20, nullable = false)
    @Builder.Default
    private AdminLevel level = AdminLevel.ADMIN;

    @Column(name = "profileImage")
    private Long profileImage;

    @Column(name = "createdAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "createdBy")
    private Integer createdBy;

    @Column(name = "deletedAt")
    private LocalDateTime deletedAt;

    @Column(name = "deletedBy")
    private Integer deletedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 삭제된 관리자 여부 확인
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 최고 관리자 여부 확인
     */
    public boolean isSuperAdmin() {
        return AdminLevel.SUPER_ADMIN.equals(level);
    }
}
