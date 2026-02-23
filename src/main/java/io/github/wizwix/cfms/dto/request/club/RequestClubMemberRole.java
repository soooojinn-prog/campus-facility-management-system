package io.github.wizwix.cfms.dto.request.club;

import io.github.wizwix.cfms.model.enums.ClubRole;

/// 동아리 부원 역할 변경
///
/// @param clubRole [ClubRole] 새 역할
// User ID는 URL에 있음
public record RequestClubMemberRole(ClubRole clubRole) {}
