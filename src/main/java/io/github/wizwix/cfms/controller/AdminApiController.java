package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.dto.request.admin.RequestAdminClubStatusUpdate;
import io.github.wizwix.cfms.dto.request.admin.RequestAdminReservationStatus;
import io.github.wizwix.cfms.dto.response.club.ResponseClubDetail;
import io.github.wizwix.cfms.dto.response.club.ResponseClubList;
import io.github.wizwix.cfms.dto.response.reservation.ResponseReservation;
import io.github.wizwix.cfms.model.enums.ClubStatus;
import io.github.wizwix.cfms.model.enums.ReservationStatus;
import io.github.wizwix.cfms.service.iface.IClubService;
import io.github.wizwix.cfms.service.iface.IReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/// 관리자 전용 API 컨트롤러
///
/// 권한: SecurityConfig에서 /api/admin/** → hasRole("ADMIN") 로 보호됨
/// 프론트: AdminPage.jsx에서 호출 (fetchAdminReservations, updateAdminReservationStatus)
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController {
  private final IClubService clubService;
  private final IReservationService reservationService;

  @GetMapping("/clubs")
  public ResponseEntity<List<ResponseClubList>> getClubs(@RequestParam(defaultValue = "PENDING") ClubStatus status) {
    return ResponseEntity.ok(clubService.getClubListByStatus(status));
  }

  /// 시설 예약 목록 조회 — 상태별 필터링 (기본 PENDING)
  /// 프론트: AdminPage.jsx → ReservationTab에서 fetchAdminReservations() 호출
  /// 쿼리 예) GET /api/admin/reservations?status=PENDING
  /// Spring이 문자열 "PENDING"을 ReservationStatus.PENDING enum으로 자동 변환
  @GetMapping("/reservations")
  public ResponseEntity<List<ResponseReservation>> getReservations(@RequestParam(defaultValue = "PENDING") ReservationStatus status) {
    return ResponseEntity.ok(reservationService.getReservationsByStatus(status));
  }

  @PatchMapping("/clubs/{slug}/status")
  public ResponseEntity<ResponseClubDetail> updateClubStatus(@PathVariable String slug, @RequestBody RequestAdminClubStatusUpdate req, Authentication auth) {
    return ResponseEntity.ok(clubService.updateClubStatus(slug, req.status(), req.reason(), auth.getName()));
  }

  /// 시설 예약 승인/거절 — PENDING 상태만 처리 가능
  /// 프론트: AdminPage.jsx → ReservationStatusModal에서 updateAdminReservationStatus() 호출
  /// 요청 바디 예) { "status": "REJECTED", "rejectReason": "사유..." }
  /// auth.getName()으로 처리한 관리자 학번을 가져와서 Reservation.processedBy에 저장
  @PatchMapping("/reservations/{id}/status")
  public ResponseEntity<Void> updateReservationStatus(@PathVariable Long id, @RequestBody RequestAdminReservationStatus req, Authentication auth) {
    reservationService.updateReservationStatus(id, req.status(), req.rejectReason(), auth.getName());
    return ResponseEntity.ok().build();
  }
}
