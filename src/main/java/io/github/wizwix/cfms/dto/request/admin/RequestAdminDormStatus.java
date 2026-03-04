package io.github.wizwix.cfms.dto.request.admin;

import io.github.wizwix.cfms.model.enums.DormApplicationStatus;
import org.springframework.lang.Nullable;

/// 기숙사 신청 상태 변경 요청 (관리자)
/// 프론트(AdminPage.jsx DormStatusModal)에서 { status, rejectReason } 형태로 전송
///
/// @param status       [DormApplicationStatus] APPROVED 또는 REJECTED
/// @param rejectReason [String] 거절 시 사유 (승인 시 null)
public record RequestAdminDormStatus(DormApplicationStatus status, @Nullable String rejectReason) {}
