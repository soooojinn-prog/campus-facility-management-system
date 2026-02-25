package io.github.wizwix.cfms.dto.response.reservation;

import io.github.wizwix.cfms.model.enums.ReservationStatus;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

/// 시설 예약 정보
///
/// @param id           [Long] 예약 ID
/// @param roomCode     [String] 방 번호 (e.g. '101호')
/// @param buildingName [String] 건물 이름
/// @param userName     [String] 예약자 이름
/// @param clubName     [String] 동아리 이름 (없으면 null)
/// @param startTime    [LocalDateTime] 이용 시작 시각
/// @param endTime      [LocalDateTime] 이용 종료 시각
/// @param status       [ReservationStatus] 예약 상태
/// @param purpose      [String] 대여 목적
/// @param rejectReason [String] 거절 사유
/// @param createdAt    [LocalDateTime] 신청 시각
public record ResponseReservation(Long id, String roomCode, String buildingName, String userName,
                                  @Nullable String clubName, LocalDateTime startTime, LocalDateTime endTime,
                                  ReservationStatus status, String purpose, @Nullable String rejectReason,
                                  LocalDateTime createdAt) {}
