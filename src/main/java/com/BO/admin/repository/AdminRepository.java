package com.BO.admin.repository;

import com.BO.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 관리자 Repository
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

    /**
     * 로그인 ID로 관리자 찾기
     */
    Optional<Admin> findByLoginId(String loginId);

    /**
     * 로그인 ID 존재 여부 확인
     */
    boolean existsByLoginId(String loginId);

    /**
     * 삭제되지 않은 관리자 찾기
     */
    Optional<Admin> findByLoginIdAndDeletedAtIsNull(String loginId);
}
