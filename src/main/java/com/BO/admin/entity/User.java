package com.BO.admin.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 엔티티
 * 테이블: TB_USER
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
    @Column(name = "user_seq")
    private Integer userSeq;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Builder.Default
    @Column(name = "language", length = 10)
    private String language = "ko";

    @Builder.Default
    @Column(name = "subscribe_in_service", length = 1)
    private String subscribeInService = "N";

    @Column(name = "access_token", length = 500)
    private String accessToken;

    @Column(name = "register_date", nullable = false, updatable = false)
    private LocalDateTime registerDate;

    @Column(name = "modify_date", nullable = false)
    private LocalDateTime modifyDate;

    @PrePersist
    protected void onCreate() {
        registerDate = LocalDateTime.now();
        modifyDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifyDate = LocalDateTime.now();
    }

    /**
     * 구독 여부 확인
     */
    public boolean isSubscribed() {
        return "Y".equals(subscribeInService);
    }
}
