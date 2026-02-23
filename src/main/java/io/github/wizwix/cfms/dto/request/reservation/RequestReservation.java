package io.github.wizwix.cfms.dto.request.reservation;

import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

/// 공실 예약 요청
///
/// @param roomId    [String] 방 ID
/// @param clubId    [Nullable] [String] 동아리의 예약인 경우, 동아리 ID
/// @param startTime [LocalDateTime] 대여 시작 시각
/// @param endTime   [LocalDateTime] 대여 종료 시각
/// @param purpose   [String] 대여 이유
public record RequestReservation(Long roomId, @Nullable Long clubId, LocalDateTime startTime, LocalDateTime endTime,
                                 String purpose) {}
