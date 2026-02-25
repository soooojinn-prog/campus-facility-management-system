package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.request.counseling.RequestCounselingReservation;
import io.github.wizwix.cfms.dto.response.counseling.ResponseCounselor;
import io.github.wizwix.cfms.dto.response.counseling.ResponseCounselingReservation;
import io.github.wizwix.cfms.model.Counselor;
import io.github.wizwix.cfms.model.CounselingReservation;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.enums.CounselingDepartment;
import io.github.wizwix.cfms.model.enums.ReservationStatus;
import io.github.wizwix.cfms.repo.CounselingReservationRepository;
import io.github.wizwix.cfms.repo.CounselorRepository;
import io.github.wizwix.cfms.service.iface.ICounselingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CounselingService implements ICounselingService {
  private final CounselorRepository counselorRepo;
  private final CounselingReservationRepository reservationRepo;

  /// 부서별 상담사 목록
  @Override
  @Transactional(readOnly = true)
  public List<ResponseCounselor> getCounselors(CounselingDepartment dept) {
    List<Counselor> counselors = dept != null
        ? counselorRepo.findByDepartment(dept)
        : counselorRepo.findAll();
    return counselors.stream()
        .map(c -> new ResponseCounselor(c.getId(), c.getName(), c.getDepartment(), c.getPosition(), c.getSpecialization()))
        .toList();
  }

  /// 해당 상담사+날짜의 예약 현황 — 공개 API (타임라인 표시용)
  /// CANCELLED 제외, PENDING/APPROVED만 반환
  @Override
  @Transactional(readOnly = true)
  public List<ResponseCounselingReservation> getSlots(Long counselorId, LocalDate date) {
    Counselor counselor = counselorRepo.findById(counselorId)
        .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다."));
    List<CounselingReservation> reservations = reservationRepo.findByCounselorAndDate(counselor, date);
    return reservations.stream()
        .filter(r -> r.getStatus() != ReservationStatus.CANCELLED && r.getStatus() != ReservationStatus.REJECTED)
        .map(r -> toResponse(r))
        .toList();
  }

  /// 상담 예약 신청 — 시간 충돌 검사 후 PENDING 상태로 생성
  @Override
  public ResponseCounselingReservation createReservation(User user, RequestCounselingReservation req) {
    Counselor counselor = counselorRepo.findById(req.counselorId())
        .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다."));

    // 시간 유효성 검사
    if (req.startTime().isAfter(req.endTime()) || req.startTime().equals(req.endTime())) {
      throw new IllegalArgumentException("시작 시간은 종료 시간보다 빨라야 합니다.");
    }
    if (req.startTime().isBefore(LocalTime.of(9, 0)) || req.endTime().isAfter(LocalTime.of(17, 0))) {
      throw new IllegalArgumentException("상담 가능 시간은 09:00 ~ 17:00 입니다.");
    }
    if (req.date().isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("과거 날짜에는 예약할 수 없습니다.");
    }

    // 시간 충돌 검사 — 해당 상담사의 해당 날짜 기존 예약과 겹치는지 확인
    List<CounselingReservation> existing = reservationRepo.findByCounselorAndDate(counselor, req.date());
    for (CounselingReservation r : existing) {
      if (r.getStatus() == ReservationStatus.CANCELLED || r.getStatus() == ReservationStatus.REJECTED) continue;
      if (req.startTime().isBefore(r.getEndTime()) && req.endTime().isAfter(r.getStartTime())) {
        throw new IllegalArgumentException("해당 시간에 이미 예약이 있습니다. (" + r.getStartTime() + " ~ " + r.getEndTime() + ")");
      }
    }

    CounselingReservation reservation = new CounselingReservation();
    reservation.setCounselor(counselor);
    reservation.setUser(user);
    reservation.setDate(req.date());
    reservation.setStartTime(req.startTime());
    reservation.setEndTime(req.endTime());
    reservation.setStatus(ReservationStatus.PENDING);
    reservation.setTopic(req.topic());
    reservation.setMemo(req.memo());
    reservation.setCreatedAt(LocalDateTime.now());
    reservationRepo.save(reservation);

    return toResponse(reservation);
  }

  /// 내 상담 예약 목록 — 마이페이지 상담 탭
  @Override
  @Transactional(readOnly = true)
  public List<ResponseCounselingReservation> getMyReservations(User user) {
    List<ReservationStatus> visibleStatuses = List.of(
        ReservationStatus.PENDING, ReservationStatus.APPROVED, ReservationStatus.REJECTED);
    List<CounselingReservation> reservations = reservationRepo.findByUserAndStatusIn(user, visibleStatuses);
    return reservations.stream().map(this::toResponse).toList();
  }

  /// 상담 예약 취소 — PENDING만 취소 가능
  @Override
  public void cancelReservation(User user, Long id) {
    CounselingReservation reservation = reservationRepo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
    if (!reservation.getUser().getId().equals(user.getId())) {
      throw new IllegalArgumentException("본인의 예약만 취소할 수 있습니다.");
    }
    if (reservation.getStatus() != ReservationStatus.PENDING) {
      throw new IllegalArgumentException("대기 중인 예약만 취소할 수 있습니다.");
    }
    reservation.setStatus(ReservationStatus.CANCELLED);
    reservationRepo.save(reservation);
  }

  private ResponseCounselingReservation toResponse(CounselingReservation r) {
    return new ResponseCounselingReservation(
        r.getId(),
        r.getCounselor().getName(),
        r.getCounselor().getDepartment().name(),
        r.getDate(),
        r.getStartTime(),
        r.getEndTime(),
        r.getStatus(),
        r.getTopic(),
        r.getMemo(),
        r.getCreatedAt()
    );
  }
}
