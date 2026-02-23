package io.github.wizwix.cfms.dto.request.club;

/// 동아리 정보 갱신
///
/// @param name        [String] 새 동아리 이름
/// @param description [String] 새 동아리 설명
/// @param autoApprove [Boolean] 자동 가입 허가 여부
public record RequestClubUpdate(String name, String description, Boolean autoApprove) {}
