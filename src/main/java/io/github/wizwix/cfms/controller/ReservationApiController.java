package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.dto.request.reservation.RequestReservation;
import io.github.wizwix.cfms.dto.response.reservation.ResponseReservation;
import io.github.wizwix.cfms.service.iface.IReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/// 시설 예약 API 컨트롤러
///
/// 인증 방식:
///   Authentication auth 파라미터 사용 — auth.getName()으로 학번(userNumber) 추출
///   ※ @AuthenticationPrincipal UserDetails 사용 불가 (JWT 필터가 principal을 String으로 설정)
///   JwtAuthenticationProcessingFilter에서 UsernamePasswordAuthenticationToken(username, null, authorities)
///   형태로 SecurityContext에 저장하므로, principal이 String(학번)임
///
/// 권한:
///   GET /api/reservations?roomId=&date= → SecurityConfig에서 permitAll (비로그인도 타임라인 조회 가능)
///   POST, DELETE, GET /me → authenticated (JWT 쿠키 필요)
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationApiController {
  private final IReservationService reservationService;

  /// 시설 예약 신청 — PENDING 상태로 생성, 관리자 승인 후 APPROVED
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseReservation createReservation(Authentication auth, @RequestBody RequestReservation req) {
    return reservationService.createReservation(auth.getName(), req);
  }

  /// 예약 취소 — PENDING 상태인 본인 예약만 취소 가능
  @DeleteMapping("/{id}")
  public void deleteReservation(Authentication auth, @PathVariable Long id) {
    reservationService.cancelReservation(auth.getName(), id);
  }

  /// 내 예약 내역 조회 — 마이페이지 '강의실 예약' 탭에서 사용
  @GetMapping("/me")
  public List<ResponseReservation> myReservations(Authentication auth) {
    return reservationService.getMyReservations(auth.getName());
  }

  /// 호실별 예약 현황 조회 (공개) — 건물 상세 페이지 타임라인 UI에서 사용
  /// SecurityConfig에서 GET "/api/reservations" 를 permitAll로 설정하여 비로그인 접근 허용
  @GetMapping
  public List<ResponseReservation> roomReservations(@RequestParam Long roomId, @RequestParam LocalDate date) {
    return reservationService.getRoomReservations(roomId, date);
  }
}
