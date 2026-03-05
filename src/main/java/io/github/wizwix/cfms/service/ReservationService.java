package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.request.reservation.RequestReservation;
import io.github.wizwix.cfms.dto.response.reservation.ResponseReservation;
import io.github.wizwix.cfms.model.Reservation;
import io.github.wizwix.cfms.model.Room;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.enums.ReservationStatus;
import io.github.wizwix.cfms.repo.ReservationRepository;
import io.github.wizwix.cfms.repo.RoomRepository;
import io.github.wizwix.cfms.repo.UserRepository;
import io.github.wizwix.cfms.service.iface.IReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/// 시설 예약 비즈니스 로직
/// - createReservation: 시간 충돌 검사 후 PENDING 상태로 저장 (관리자 승인 대기)
/// - cancelReservation: PENDING 상태 + 본인 예약만 취소 가능
/// - getRoomReservations: 타임라인 UI용 (공개) — PENDING/APPROVED만 표시
/// - getMyReservations: 마이페이지용 (인증) — REJECTED도 포함하여 사용자에게 결과 알림
/// - getReservationsByStatus: 관리자용 — 상태별 전체 예약 조회
/// - updateReservationStatus: 관리자용 — 예약 승인/거절 (PENDING만 처리 가능)
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService implements IReservationService {
  /// ACTIVE_STATUSES: 시간 충돌 검사 + 타임라인 조회에 사용
  ///   PENDING/APPROVED 상태인 예약만 "활성 예약"으로 간주
  ///   CANCELLED/REJECTED는 충돌 대상에서 제외
  private static final List<ReservationStatus> ACTIVE_STATUSES = List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED);
  /// VISIBLE_STATUSES: 마이페이지 내 예약 내역에 사용
  ///   REJECTED도 포함하여 거절 사유를 사용자에게 보여줌
  ///   CANCELLED는 사용자가 직접 취소한 것이므로 제외 (필요 시 포함 가능)
  private static final List<ReservationStatus> VISIBLE_STATUSES = List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED, ReservationStatus.REJECTED);
  private final ReservationRepository reservationRepo;
  private final RoomRepository roomRepo;
  private final UserRepository userRepo;

  @Override
  public void cancelReservation(String userNumber, Long reservationId) {
    Reservation reservation = reservationRepo.findById(reservationId).orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
    if (!reservation.getUser().getNumber().equals(userNumber)) {
      throw new IllegalStateException("본인의 예약만 취소할 수 있습니다.");
    }
    if (reservation.getStatus() != ReservationStatus.PENDING) {
      throw new IllegalArgumentException("대기 중인 예약만 취소할 수 있습니다.");
    }
    reservation.setStatus(ReservationStatus.CANCELLED);
    reservation.setUpdatedAt(LocalDateTime.now());
    reservationRepo.save(reservation);
  }

  @Override
  public ResponseReservation createReservation(String userNumber, RequestReservation req) {
    User user = userRepo.findByNumber(userNumber).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    Room room = roomRepo.findById(req.roomId()).orElseThrow(() -> new IllegalArgumentException("호실을 찾을 수 없습니다."));

    /// 시간 충돌 검사 — "겹침(overlap)" 조건:
    ///   기존 예약의 startTime < 새 예약의 endTime AND 기존 예약의 endTime > 새 예약의 startTime
    ///   예) 기존 10:00~12:00 vs 새 11:00~13:00 → 겹침(11~12)
    ///   예) 기존 10:00~12:00 vs 새 12:00~14:00 → 안겹침(경계는 허용)
    ///   주의: JPA 메서드명에서 파라미터 순서가 직관과 다름
    ///         ..LessThan(endTime) = DB.startTime < endTime
    ///         ..GreaterThan(startTime) = DB.endTime > startTime
    List<Reservation> conflicts = reservationRepo.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusIn(room.getId(), req.endTime(), req.startTime(), ACTIVE_STATUSES);
    if (!conflicts.isEmpty()) {
      throw new IllegalStateException("선택한 시간에 이미 예약이 존재합니다.");
    }

    Reservation reservation = new Reservation();
    reservation.setRoom(room);
    reservation.setUser(user);
    reservation.setStartTime(req.startTime());
    reservation.setEndTime(req.endTime());
    reservation.setPurpose(req.purpose());
    reservation.setStatus(ReservationStatus.PENDING);
    reservation.setCreatedAt(LocalDateTime.now());
    reservation.setUpdatedAt(LocalDateTime.now());

    Reservation saved = reservationRepo.save(reservation);
    return toResponse(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ResponseReservation> getMyReservations(String userNumber) {
    return reservationRepo.findByUserNumberAndStatusIn(userNumber, VISIBLE_STATUSES).stream().map(this::toResponse).toList();
  }

  /// 관리자용 — 상태별 전체 예약 조회
  /// AdminPage.jsx → ReservationTab에서 PENDING 목록을 가져올 때 사용
  @Override
  @Transactional(readOnly = true)
  public List<ResponseReservation> getReservationsByStatus(ReservationStatus status) {
    return reservationRepo.findByStatus(status).stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ResponseReservation> getRoomReservations(Long roomId, LocalDate date) {
    LocalDateTime dayStart = date.atStartOfDay();
    LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
    return reservationRepo.findByRoomIdAndStartTimeBetweenAndStatusIn(roomId, dayStart, dayEnd, ACTIVE_STATUSES).stream().map(this::toResponse).toList();
  }

  /// 관리자용 — 예약 승인/거절 처리
  /// 흐름: AdminPage.jsx → ReservationStatusModal → PATCH /api/admin/reservations/{id}/status
  ///
  /// 처리 로직:
  ///   1. PENDING 상태가 아니면 거부 (이미 처리된 예약 재처리 방지)
  ///   2. 승인(APPROVED): rejectReason = null
  ///      거절(REJECTED): rejectReason = 관리자가 입력한 사유 저장
  ///   3. processedBy = 처리한 관리자 (Reservation 엔티티에 이미 존재하는 필드)
  ///
  /// 거절 사유 확인 경로: 학생 마이페이지 → 강의실 예약 탭 → REJECTED 상태 옆에 표시
  ///   (별도 알림 시스템 없이, ResponseReservation.rejectReason 필드로 전달)
  @Override
  public void updateReservationStatus(Long id, ReservationStatus status, String rejectReason, String adminNumber) {
    Reservation reservation = reservationRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
    if (reservation.getStatus() != ReservationStatus.PENDING) {
      throw new IllegalArgumentException("대기 중인 예약만 처리할 수 있습니다.");
    }
    // 승인 시 같은 호실·겹치는 시간에 이미 승인된 예약이 있으면 거부
    if (status == ReservationStatus.APPROVED) {
      List<Reservation> conflicts = reservationRepo.findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusIn(
          reservation.getRoom().getId(), reservation.getEndTime(), reservation.getStartTime(),
          List.of(ReservationStatus.APPROVED));
      if (!conflicts.isEmpty()) {
        throw new IllegalStateException("해당 시간대에 이미 승인된 예약이 있습니다.");
      }
    }

    User admin = userRepo.findByNumber(adminNumber).orElseThrow(() -> new IllegalArgumentException("관리자를 찾을 수 없습니다."));
    reservation.setStatus(status);
    reservation.setRejectReason(status == ReservationStatus.REJECTED ? rejectReason : null);
    reservation.setProcessedBy(admin);
    reservation.setUpdatedAt(LocalDateTime.now());
    reservationRepo.save(reservation);
  }

  private ResponseReservation toResponse(Reservation r) {
    return new ResponseReservation(r.getId(), r.getRoom().getName(), r.getRoom().getBuilding().getName(), r.getUser().getName(), r.getClub() != null ? r.getClub().getName() : null, r.getStartTime(), r.getEndTime(), r.getStatus(), r.getPurpose(), r.getRejectReason(), r.getCreatedAt());
  }
}
