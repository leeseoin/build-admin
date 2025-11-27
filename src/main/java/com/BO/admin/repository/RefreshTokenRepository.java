package com.BO.admin.repository;

import com.BO.admin.entity.RefreshToken;
import com.BO.admin.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Refresh Token Repository
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 문자열로 찾기
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자 ID와 타입으로 찾기
     */
    Optional<RefreshToken> findByUserIdAndUserType(Long userId, UserType userType);

    /**
     * 관리자 ID와 타입으로 찾기
     */
    Optional<RefreshToken> findByAdminIdAndUserType(Integer adminId, UserType userType);

    /**
     * 사용자 ID로 삭제
     */
    void deleteByUserId(Long userId);

    /**
     * 관리자 ID로 삭제
     */
    void deleteByAdminId(Integer adminId);

    /**
     * 토큰 문자열로 삭제
     */
    void deleteByToken(String token);

    /**
     * 만료된 토큰 삭제
     */
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
