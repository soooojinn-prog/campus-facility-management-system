package io.github.wizwix.cfms.dto.response.auth;

import io.github.wizwix.cfms.model.enums.UserRole;

import java.time.LocalDateTime;

/// 사용자 정보
///
/// @param id         [Long] 사용자 ID (내부 인덱싱용)
/// @param name       [String] 사용자 이름
/// @param userNumber [String] 학번 or 교번
/// @param email      [String] 이메일
/// @param role       [UserRole] 사용자 유형
/// @param createdAt  [LocalDateTime] 가입 일시
public record ResponseUserProfile(Long id, String name, String userNumber, String email, UserRole role,
                                  LocalDateTime createdAt) {}
