package io.github.wizwix.cfms.dto.request.auth;

import io.github.wizwix.cfms.model.enums.Gender;
import jakarta.validation.constraints.NotNull;

/// 회원 가입 요청
///
/// @param userNumber [String] 교번 or 학번
/// @param name       [String] 이름
/// @param password   [String] 비밀번호
/// @param email      [String] 이메일 주소
/// @param gender     [Gender] 성별
/// @param role       [String] 역할 ("ROLE_STUDENT" | "ROLE_PROFESSOR") — null이면 ROLE_STUDENT
public record RequestRegister(String userNumber, String name, String password, String email,
                              @NotNull Gender gender, String role) {}
