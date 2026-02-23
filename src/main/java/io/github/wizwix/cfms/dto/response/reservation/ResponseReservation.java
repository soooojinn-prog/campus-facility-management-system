package io.github.wizwix.cfms.dto.response.reservation;

import io.github.wizwix.cfms.model.enums.ReservationStatus;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

/// 예약 정보
///
/// @param id           [Long] 대여 ID (내부 인덱싱용)
/// @param roomCode     [String] 방 정식 명칭 (e.g. 'IT대학동 104호')
/// @param clubName     [String] (동아리 예약인 경우) 대여한 동아리 이름
/// @param userName     [String] 대여한 사람의 이름
/// @param startTime    [LocalDateTime] 대여 시작 시각
/// @param endTime      [LocalDateTime] 대여 종료 시각
/// @param status       [ReservationStatus] 대여 상태
/// @param purpose      [String] 대여 목적
/// @param rejectReason [String] (대여 거부 시) 거부 사유
public record ResponseReservation(Long id, String roomCode, String clubName, String userName, LocalDateTime startTime,
                                  LocalDateTime endTime, ReservationStatus status, String purpose,
                                  @Nullable String rejectReason) {}
