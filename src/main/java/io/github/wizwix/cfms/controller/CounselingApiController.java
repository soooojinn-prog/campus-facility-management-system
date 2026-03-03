package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.dto.request.counseling.RequestCounselingReservation;
import io.github.wizwix.cfms.dto.response.counseling.ResponseCounselingReservation;
import io.github.wizwix.cfms.dto.response.counseling.ResponseCounselor;
import io.github.wizwix.cfms.model.enums.CounselingDepartment;
import io.github.wizwix.cfms.service.iface.ICounselingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/counseling")
@RequiredArgsConstructor
public class CounselingApiController {
  private final ICounselingService counselingService;

  /// 상담 예약 취소 — PENDING만 가능 (인증 필요)
  @DeleteMapping("/reservations/{id}")
  public ResponseEntity<Void> cancelReservation(@PathVariable Long id, Authentication auth) {
    counselingService.cancelReservation(auth.getName(), id);
    return ResponseEntity.noContent().build();
  }

  /// 상담 예약 신청 (인증 필요)
  @PostMapping("/reservations")
  public ResponseEntity<ResponseCounselingReservation> createReservation(@RequestBody RequestCounselingReservation req, Authentication auth) {
    return ResponseEntity.ok(counselingService.createReservation(auth.getName(), req));
  }

  /// 상담사 목록 — 부서별 필터 (공개)
  @GetMapping("/counselors")
  public ResponseEntity<List<ResponseCounselor>> getCounselors(@RequestParam(required = false) CounselingDepartment dept) {
    return ResponseEntity.ok(counselingService.getCounselors(dept));
  }

  /// 해당 상담사+날짜의 예약 현황 (공개)
  @GetMapping("/slots")
  public ResponseEntity<List<ResponseCounselingReservation>> getSlots(@RequestParam Long counselorId, @RequestParam LocalDate date) {
    return ResponseEntity.ok(counselingService.getSlots(counselorId, date));
  }

  /// 내 상담 예약 목록 (인증 필요)
  @GetMapping("/reservations/me")
  public ResponseEntity<List<ResponseCounselingReservation>> myReservations(Authentication auth) {
    return ResponseEntity.ok(counselingService.getMyReservations(auth.getName()));
  }
}
