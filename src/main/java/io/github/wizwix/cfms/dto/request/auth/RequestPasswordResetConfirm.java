package io.github.wizwix.cfms.dto.request.auth;

/// 비밀번호 초기화 요청 (3단계)
/// > `usernumber`는 숨겨진 `<input>`으로 받음
///
/// @param userNumber  [String] 교번 or 학번
/// @param token       [String] 인증 번호
/// @param newPassword [String] 새로운 비밀번호
public record RequestPasswordResetConfirm(String userNumber, String token, String newPassword) {}
