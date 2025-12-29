package com.BO.admin.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * 로그인 ID
     */
    private String loginId;

    /**
     * 비밀번호
     */
    private String password;
}
