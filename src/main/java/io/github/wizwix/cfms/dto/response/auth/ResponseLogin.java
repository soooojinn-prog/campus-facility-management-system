package io.github.wizwix.cfms.dto.response.auth;

/// 로그인 성공 시 반환 (로그인 성공 전용 레코드)
///
/// @param accessToken [String] JWT 토큰
/// @param user        [ResponseUserSimpleInfo] 사용자 정보
public record ResponseLogin(String accessToken, ResponseUserSimpleInfo user) {}
