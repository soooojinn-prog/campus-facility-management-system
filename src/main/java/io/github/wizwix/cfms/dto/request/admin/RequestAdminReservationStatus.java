package io.github.wizwix.cfms.dto.request.admin;

import io.github.wizwix.cfms.model.enums.ReservationStatus;
import org.springframework.lang.Nullable;

/// 시설 예약 상태 변경 (관리자)
///
/// @param reason [String] 상태 변경 이유
public record RequestAdminReservationStatus(ReservationStatus status, @Nullable String reason) {}
