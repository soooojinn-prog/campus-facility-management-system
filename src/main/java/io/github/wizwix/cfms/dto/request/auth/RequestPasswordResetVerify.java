package io.github.wizwix.cfms.dto.request.auth;

/// 비밀번호 초기화 요청 (2단계)
/// > `usernumber`는 숨겨진 `<input>`으로 받음
///
/// @param userNumber [String] 학번 or 교번
/// @param token      [String] 본인 확인용 인증 토큰 (임의의 숫자 6자리)
// 인증 토큰이 일치할 시 3단계로 넘어감
public record RequestPasswordResetVerify(String userNumber, String token) {}
