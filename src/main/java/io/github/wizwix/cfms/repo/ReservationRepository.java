package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.Reservation;
import io.github.wizwix.cfms.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
  /// 호실+날짜별 예약 조회 (타임라인 UI용)
  /// Between: dayStart <= start_time <= dayEnd
  List<Reservation> findByRoomIdAndStartTimeBetweenAndStatusIn(
      Long roomId, LocalDateTime dayStart, LocalDateTime dayEnd, List<ReservationStatus> statuses);

  /// 시간 충돌(overlap) 검사용 쿼리
  /// JPA 메서드 네이밍 해석:
  ///   findByRoomId                  → WHERE room_id = ?
  ///   AndStartTimeLessThan          → AND start_time < ?  (파라미터: endTime)
  ///   AndEndTimeGreaterThan         → AND end_time > ?    (파라미터: startTime)
  ///   AndStatusIn                   → AND status IN (?)
  /// 주의: 파라미터명과 컬럼명이 교차되어 헷갈릴 수 있음
  ///       "StartTimeLessThan(endTime)" = DB의 start_time이 새 예약의 endTime보다 작은 것
  List<Reservation> findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusIn(
      Long roomId, LocalDateTime endTime, LocalDateTime startTime, List<ReservationStatus> statuses);

  /// 상태별 예약 조회 — 관리자 예약 관리 화면에서 사용
  List<Reservation> findByStatus(ReservationStatus status);

  /// 내 예약 조회 — User 엔티티의 number 필드(학번/교번) 기준
  /// JPA가 Reservation → User 관계를 자동 조인하여 user.number = ? 로 변환
  List<Reservation> findByUserNumberAndStatusIn(String number, List<ReservationStatus> statuses);
}
