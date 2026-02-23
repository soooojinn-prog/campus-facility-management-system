package io.github.wizwix.cfms.dto.request.club;

/// 동아리 생성 요청
///
/// @param name        [String] 동아리 이름
/// @param slug        [String] 동아리 URL 식별자
/// @param description [String] 동아리 소개/설명문
/// @param autoApprove [Boolean] 부원 자동 가입 허가 여부
public record RequestClubCreate(String name, String slug, String description, Boolean autoApprove) {}
