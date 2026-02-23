package io.github.wizwix.cfms.dto.request.auth;

/// 비밀번호 초기화 요청 (1단계)
///
/// @param userNumber [String] 교번 or 학번
/// @param email      [String] 해당 회원의 이메일 (회원 정보와 일치할 시 이메일 전송)
// 본래라면 인증 토큰을 이메일로 보내야 하겠지만, 여기서는 임시로 인증 토큰을 콘솔에 출력할 예정
public record RequestPasswordResetSend(String userNumber, String email) {}
