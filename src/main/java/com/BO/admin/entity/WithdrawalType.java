package com.BO.admin.entity;

/**
 * 회원 탈퇴 유형
 * tb_user.withdrawalType
 */
public enum WithdrawalType {
    SELF,      // 회원 직접 탈퇴
    SYSTEM     // 시스템에 의한 탈퇴 (장기간 휴면)
}
