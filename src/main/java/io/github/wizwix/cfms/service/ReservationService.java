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
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService implements IReservationService {
  private final ReservationRepository reservationRepo;
  private final RoomRepository roomRepo;
  private final UserRepository userRepo;

  /// ACTIVE_STATUSES: 시간 충돌 검사 + 타임라인 조회에 사용
  ///   PENDING/APPROVED 상태인 예약만 "활성 예약"으로 간주
  ///   CANCELLED/REJECTED는 충돌 대상에서 제외
  private static final List<ReservationStatus> ACTIVE_STATUSES = List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED);
  /// VISIBLE_STATUSES: 마이페이지 내 예약 내역에 사용
  ///   REJECTED도 포함하여 거절 사유를 사용자에게 보여줌
  ///   CANCELLED는 사용자가 직접 취소한 것이므로 제외 (필요 시 포함 가능)
  private static final List<ReservationStatus> VISIBLE_STATUSES = List.of(ReservationStatus.PENDING, ReservationStatus.APPROVED, ReservationStatus.REJECTED);

  @Override
  public ResponseReservation createReservation(String userNumber, RequestReservation req) {
    User user = userRepo.findByNumber(userNumber)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    Room room = roomRepo.findById(req.roomId())
        .orElseThrow(() -> new IllegalArgumentException("호실을 찾을 수 없습니다."));

    /// 시간 충돌 검사 — "겹침(overlap)" 조건:
    ///   기존 예약의 startTime < 새 예약의 endTime AND 기존 예약의 endTime > 새 예약의 startTime
    ///   예) 기존 10:00~12:00 vs 새 11:00~13:00 → 겹침(11~12)
    ///   예) 기존 10:00~12:00 vs 새 12:00~14:00 → 안겹침(경계는 허용)
    ///   주의: JPA 메서드명에서 파라미터 순서가 직관과 다름
    ///         ..LessThan(endTime) = DB.startTime < endTime
    ///         ..GreaterThan(startTime) = DB.endTime > startTime
    List<Reservation> conflicts = reservationRepo
        .findByRoomIdAndStartTimeLessThanAndEndTimeGreaterThanAndStatusIn(
            room.getId(), req.endTime(), req.startTime(), ACTIVE_STATUSES);
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
  public void cancelReservation(String userNumber, Long reservationId) {
    Reservation reservation = reservationRepo.findById(reservationId)
        .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
    if (!reservation.getUser().getNumber().equals(userNumber)) {
      throw new IllegalStateException("본인의 예약만 취소할 수 있습니다.");
    }
    if (reservation.getStatus() != ReservationStatus.PENDING) {
      throw new IllegalStateException("승인 대기 중인 예약만 취소할 수 있습니다.");
    }
    reservation.setStatus(ReservationStatus.CANCELLED);
    reservation.setUpdatedAt(LocalDateTime.now());
    reservationRepo.save(reservation);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ResponseReservation> getMyReservations(String userNumber) {
    return reservationRepo.findByUserNumberAndStatusIn(userNumber, VISIBLE_STATUSES).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ResponseReservation> getRoomReservations(Long roomId, LocalDate date) {
    LocalDateTime dayStart = date.atStartOfDay();
    LocalDateTime dayEnd = date.atTime(LocalTime.MAX);
    return reservationRepo.findByRoomIdAndStartTimeBetweenAndStatusIn(roomId, dayStart, dayEnd, ACTIVE_STATUSES).stream()
        .map(this::toResponse)
        .toList();
  }

  private ResponseReservation toResponse(Reservation r) {
    return new ResponseReservation(
        r.getId(),
        r.getRoom().getName(),
        r.getRoom().getBuilding().getName(),
        r.getUser().getName(),
        r.getClub() != null ? r.getClub().getName() : null,
        r.getStartTime(),
        r.getEndTime(),
        r.getStatus(),
        r.getPurpose(),
        r.getRejectReason(),
        r.getCreatedAt()
    );
  }
}
