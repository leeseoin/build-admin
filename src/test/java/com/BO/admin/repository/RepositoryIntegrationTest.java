package com.BO.admin.repository;

import com.BO.admin.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository 통합 테스트
 * - User, Admin, RefreshToken Repository 테스트
 * - H2 in-memory DB 사용
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Repository 통합 테스트")
class RepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // ========================================
    // UserRepository 테스트
    // ========================================

    @Test
    @DisplayName("일반 사용자 저장 및 조회")
    void saveAndFindUser_Success() {
        // given
        User user = User.builder()
                .loginId("testuser@example.com")
                .userName("테스트유저")
                .password("encrypted_password")
                .socialLoginYn(false)
                .registeredPath(RegisteredPath.EMAIL)
                .language("KOR")
                .subscribeInService(true)
                .build();

        // when
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getUserId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getLoginId()).isEqualTo("testuser@example.com");
        assertThat(foundUser.get().getUserName()).isEqualTo("테스트유저");
        assertThat(foundUser.get().isSocialUser()).isFalse();
    }

    @Test
    @DisplayName("소셜 로그인 사용자 저장 및 조회")
    void saveSocialUser_Success() {
        // given
        User user = User.builder()
                .loginId("google_123456789")
                .userName("구글유저")
                .password("N/A")
                .socialLoginYn(true)
                .registeredPath(RegisteredPath.SOCIAL)
                .language("ENG")
                .subscribeInService(false)
                .build();

        // when
        User savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.isSocialUser()).isTrue();
        assertThat(savedUser.getRegisteredPath()).isEqualTo(RegisteredPath.SOCIAL);
    }

    @Test
    @DisplayName("loginId로 사용자 찾기")
    void findByLoginId_Success() {
        // given
        User user = User.builder()
                .loginId("findme@example.com")
                .userName("찾아줘")
                .password("password123")
                .socialLoginYn(false)
                .registeredPath(RegisteredPath.EMAIL)
                .build();
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByLoginId("findme@example.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserName()).isEqualTo("찾아줘");
    }

    @Test
    @DisplayName("loginId 존재 여부 확인")
    void existsByLoginId_Success() {
        // given
        User user = User.builder()
                .loginId("exists@example.com")
                .userName("존재함")
                .password("password123")
                .socialLoginYn(false)
                .registeredPath(RegisteredPath.EMAIL)
                .build();
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByLoginId("exists@example.com");
        boolean notExists = userRepository.existsByLoginId("notexists@example.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("탈퇴하지 않은 사용자 조회")
    void findByLoginIdAndWithdrawalAtIsNull_Success() {
        // given: 탈퇴한 사용자
        User withdrawnUser = User.builder()
                .loginId("withdrawn@example.com")
                .userName("탈퇴유저")
                .password("password123")
                .socialLoginYn(false)
                .registeredPath(RegisteredPath.EMAIL)
                .withdrawalType(WithdrawalType.SELF)
                .withdrawalAt(LocalDateTime.now())
                .build();
        userRepository.save(withdrawnUser);

        // given: 활성 사용자
        User activeUser = User.builder()
                .loginId("active@example.com")
                .userName("활성유저")
                .password("password123")
                .socialLoginYn(false)
                .registeredPath(RegisteredPath.EMAIL)
                .build();
        userRepository.save(activeUser);

        // when
        Optional<User> foundWithdrawn = userRepository.findByLoginIdAndWithdrawalAtIsNull("withdrawn@example.com");
        Optional<User> foundActive = userRepository.findByLoginIdAndWithdrawalAtIsNull("active@example.com");

        // then
        assertThat(foundWithdrawn).isEmpty(); // 탈퇴한 사용자는 조회되지 않음
        assertThat(foundActive).isPresent(); // 활성 사용자만 조회됨
    }

    @Test
    @DisplayName("사용자 탈퇴 처리")
    void withdrawUser_Success() {
        // given
        User user = User.builder()
                .loginId("todelete@example.com")
                .userName("삭제예정")
                .password("password123")
                .socialLoginYn(false)
                .registeredPath(RegisteredPath.EMAIL)
                .build();
        User savedUser = userRepository.save(user);

        // when: 탈퇴 처리
        savedUser.setWithdrawalType(WithdrawalType.SELF);
        savedUser.setWithdrawalAt(LocalDateTime.now());
        userRepository.save(savedUser);

        // then
        User withdrawnUser = userRepository.findById(savedUser.getUserId()).get();
        assertThat(withdrawnUser.isWithdrawn()).isTrue();
        assertThat(withdrawnUser.getWithdrawalType()).isEqualTo(WithdrawalType.SELF);
    }

    // ========================================
    // AdminRepository 테스트
    // ========================================

    @Test
    @DisplayName("관리자 저장 및 조회")
    void saveAndFindAdmin_Success() {
        // given
        Admin admin = Admin.builder()
                .loginId("admin@example.com")
                .password("encrypted_admin_password")
                .name("관리자1")
                .level(AdminLevel.ADMIN)
                .memo("테스트 관리자")
                .build();

        // when
        Admin savedAdmin = adminRepository.save(admin);
        Optional<Admin> foundAdmin = adminRepository.findById(savedAdmin.getAdminId());

        // then
        assertThat(foundAdmin).isPresent();
        assertThat(foundAdmin.get().getLoginId()).isEqualTo("admin@example.com");
        assertThat(foundAdmin.get().getName()).isEqualTo("관리자1");
        assertThat(foundAdmin.get().getLevel()).isEqualTo(AdminLevel.ADMIN);
    }

    @Test
    @DisplayName("SUPER_ADMIN 저장 및 확인")
    void saveSuperAdmin_Success() {
        // given
        Admin superAdmin = Admin.builder()
                .loginId("superadmin@example.com")
                .password("encrypted_super_password")
                .name("최고관리자")
                .level(AdminLevel.SUPER_ADMIN)
                .build();

        // when
        Admin savedAdmin = adminRepository.save(superAdmin);

        // then
        assertThat(savedAdmin.isSuperAdmin()).isTrue();
        assertThat(savedAdmin.getLevel()).isEqualTo(AdminLevel.SUPER_ADMIN);
    }

    @Test
    @DisplayName("loginId로 관리자 찾기")
    void findAdminByLoginId_Success() {
        // given
        Admin admin = Admin.builder()
                .loginId("findadmin@example.com")
                .password("password123")
                .name("찾을관리자")
                .level(AdminLevel.MANAGER)
                .build();
        adminRepository.save(admin);

        // when
        Optional<Admin> foundAdmin = adminRepository.findByLoginId("findadmin@example.com");

        // then
        assertThat(foundAdmin).isPresent();
        assertThat(foundAdmin.get().getName()).isEqualTo("찾을관리자");
        assertThat(foundAdmin.get().getLevel()).isEqualTo(AdminLevel.MANAGER);
    }

    @Test
    @DisplayName("관리자 loginId 존재 여부 확인")
    void adminExistsByLoginId_Success() {
        // given
        Admin admin = Admin.builder()
                .loginId("existsadmin@example.com")
                .password("password123")
                .name("존재관리자")
                .level(AdminLevel.ADMIN)
                .build();
        adminRepository.save(admin);

        // when
        boolean exists = adminRepository.existsByLoginId("existsadmin@example.com");
        boolean notExists = adminRepository.existsByLoginId("notexists@example.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("삭제되지 않은 관리자 조회")
    void findAdminByLoginIdAndDeletedAtIsNull_Success() {
        // given: 삭제된 관리자
        Admin deletedAdmin = Admin.builder()
                .loginId("deleted@example.com")
                .password("password123")
                .name("삭제관리자")
                .level(AdminLevel.ADMIN)
                .deletedAt(LocalDateTime.now())
                .deletedBy(1)
                .build();
        adminRepository.save(deletedAdmin);

        // given: 활성 관리자
        Admin activeAdmin = Admin.builder()
                .loginId("active_admin@example.com")
                .password("password123")
                .name("활성관리자")
                .level(AdminLevel.ADMIN)
                .build();
        adminRepository.save(activeAdmin);

        // when
        Optional<Admin> foundDeleted = adminRepository.findByLoginIdAndDeletedAtIsNull("deleted@example.com");
        Optional<Admin> foundActive = adminRepository.findByLoginIdAndDeletedAtIsNull("active_admin@example.com");

        // then
        assertThat(foundDeleted).isEmpty(); // 삭제된 관리자는 조회되지 않음
        assertThat(foundActive).isPresent(); // 활성 관리자만 조회됨
    }

    // ========================================
    // RefreshTokenRepository 테스트
    // ========================================

    @Test
    @DisplayName("일반 사용자 Refresh Token 저장 및 조회")
    void saveUserRefreshToken_Success() {
        // given: 사용자 생성
        User user = User.builder()
                .loginId("tokenuser@example.com")
                .userName("토큰유저")
                .password("password123")
                .socialLoginYn(false)
                .registeredPath(RegisteredPath.EMAIL)
                .build();
        User savedUser = userRepository.save(user);

        // given: Refresh Token 생성
        RefreshToken refreshToken = RefreshToken.builder()
                .token("user_refresh_token_12345")
                .userId(savedUser.getUserId())
                .userType(UserType.USER)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        // when
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken("user_refresh_token_12345");

        // then
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(foundToken.get().isUserToken()).isTrue();
        assertThat(foundToken.get().isAdminToken()).isFalse();
    }

    @Test
    @DisplayName("관리자 Refresh Token 저장 및 조회")
    void saveAdminRefreshToken_Success() {
        // given: 관리자 생성
        Admin admin = Admin.builder()
                .loginId("tokenadmin@example.com")
                .password("password123")
                .name("토큰관리자")
                .level(AdminLevel.ADMIN)
                .build();
        Admin savedAdmin = adminRepository.save(admin);

        // given: Refresh Token 생성
        RefreshToken refreshToken = RefreshToken.builder()
                .token("admin_refresh_token_12345")
                .adminId(savedAdmin.getAdminId())
                .userType(UserType.ADMIN)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        // when
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken("admin_refresh_token_12345");

        // then
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getAdminId()).isEqualTo(savedAdmin.getAdminId());
        assertThat(foundToken.get().isAdminToken()).isTrue();
        assertThat(foundToken.get().isUserToken()).isFalse();
    }

    @Test
    @DisplayName("사용자별 Refresh Token 조회")
    void findRefreshTokenByUserIdAndUserType_Success() {
        // given: 사용자 생성
        User user = User.builder()
                .loginId("multitoken@example.com")
                .userName("멀티토큰")
                .password("password123")
                .socialLoginYn(false)
                .registeredPath(RegisteredPath.EMAIL)
                .build();
        User savedUser = userRepository.save(user);

        // given: Refresh Token 생성
        RefreshToken token = RefreshToken.builder()
                .token("user_specific_token")
                .userId(savedUser.getUserId())
                .userType(UserType.USER)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(token);

        // when
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByUserIdAndUserType(
                savedUser.getUserId(), UserType.USER);

        // then
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getUserId()).isEqualTo(savedUser.getUserId());
    }

    @Test
    @DisplayName("userId로 Refresh Token 삭제")
    void deleteRefreshTokenByUserId_Success() {
        // given: 사용자 및 토큰 생성
        User user = User.builder()
                .loginId("deletetoken@example.com")
                .userName("삭제토큰")
                .password("password123")
                .socialLoginYn(false)
                .registeredPath(RegisteredPath.EMAIL)
                .build();
        User savedUser = userRepository.save(user);

        RefreshToken token = RefreshToken.builder()
                .token("to_be_deleted")
                .userId(savedUser.getUserId())
                .userType(UserType.USER)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(token);

        // when
        refreshTokenRepository.deleteByUserId(savedUser.getUserId());

        // then
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken("to_be_deleted");
        assertThat(foundToken).isEmpty();
    }

    @Test
    @DisplayName("토큰 문자열로 Refresh Token 삭제")
    void deleteRefreshTokenByToken_Success() {
        // given
        RefreshToken token = RefreshToken.builder()
                .token("delete_this_token")
                .userId(1L)
                .userType(UserType.USER)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(token);

        // when
        refreshTokenRepository.deleteByToken("delete_this_token");

        // then
        Optional<RefreshToken> foundToken = refreshTokenRepository.findByToken("delete_this_token");
        assertThat(foundToken).isEmpty();
    }

    @Test
    @DisplayName("만료된 Refresh Token 삭제")
    void deleteExpiredRefreshTokens_Success() {
        // given: 만료된 토큰
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired_token")
                .userId(1L)
                .userType(UserType.USER)
                .expiresAt(LocalDateTime.now().minusDays(1)) // 어제 만료
                .build();
        refreshTokenRepository.save(expiredToken);

        // given: 유효한 토큰
        RefreshToken validToken = RefreshToken.builder()
                .token("valid_token")
                .userId(2L)
                .userType(UserType.USER)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(validToken);

        // when
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        // then
        Optional<RefreshToken> foundExpired = refreshTokenRepository.findByToken("expired_token");
        Optional<RefreshToken> foundValid = refreshTokenRepository.findByToken("valid_token");

        assertThat(foundExpired).isEmpty(); // 만료된 토큰은 삭제됨
        assertThat(foundValid).isPresent(); // 유효한 토큰은 유지됨
    }

    @Test
    @DisplayName("Refresh Token 만료 여부 확인")
    void checkRefreshTokenExpiration_Success() {
        // given: 만료된 토큰
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired")
                .userId(1L)
                .userType(UserType.USER)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        // given: 유효한 토큰
        RefreshToken validToken = RefreshToken.builder()
                .token("valid")
                .userId(2L)
                .userType(UserType.USER)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        // then
        assertThat(expiredToken.isExpired()).isTrue();
        assertThat(validToken.isExpired()).isFalse();
    }
}
