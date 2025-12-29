package com.BO.admin.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    /**
     * 로그인 ID (이메일 또는 고유 ID)
     */
    private String loginId;

    /**
     * 비밀번호
     */
    private String password;

    /**
     * 사용자 이름
     */
    private String userName;

    /**
     * 언어 설정 (KOR, ENG 등)
     */
    private String language;

    /**
     * 서비스 구독 여부
     */
    private Boolean subscribeInService;
}
