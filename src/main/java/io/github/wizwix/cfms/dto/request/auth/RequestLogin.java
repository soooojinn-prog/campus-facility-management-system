package io.github.wizwix.cfms.dto.request.auth;

/// 사용자 로그인
///
/// @param userNumber [String] 사용자 학번 or 교번
/// @param password   [String] 비밀번호
public record RequestLogin(String userNumber, String password) {}
