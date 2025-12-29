package com.BO.admin.repository;

import com.BO.admin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * 로그인 ID로 사용자 찾기
     */
    Optional<User> findByLoginId(String loginId);

    /**
     * 로그인 ID 존재 여부 확인
     */
    boolean existsByLoginId(String loginId);
}
