package io.github.wizwix.cfms.dto.request.admin;

import io.github.wizwix.cfms.model.enums.ClubStatus;
import org.springframework.lang.Nullable;

/// 동아리 상태 변경 요청 (관리자)
/// 프론트(AdminPage.jsx ClubStatusModal)에서 { status, rejectReason } 형태로 전송
///
/// @param status       [ClubStatus] APPROVED 또는 REJECTED
/// @param rejectReason [String] 거절 시 사유 (승인 시 null)
public record RequestAdminClubStatusUpdate(ClubStatus status, @Nullable String rejectReason) {}
