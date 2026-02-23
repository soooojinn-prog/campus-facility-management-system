package io.github.wizwix.cfms.dto.request.admin;

import io.github.wizwix.cfms.model.enums.ClubStatus;
import org.springframework.lang.Nullable;

/// 동아리 상태 변경 (관리자)
///
/// @param reason [String] 상태 변경 이유
public record RequestAdminClubStatusUpdate(ClubStatus status, @Nullable String reason) {}
