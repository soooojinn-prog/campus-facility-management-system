package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.dto.response.reservation.ResponseReservation;
import io.github.wizwix.cfms.service.iface.IReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomApiController {
  private final IReservationService reservationService;

  /// 특정 방의 예약 목록
  ///
  /// @deprecated `GET /api/reservations?roomId=&date=`를 사용할 것
  @GetMapping("/{roomId}/reservations")
  @Deprecated(forRemoval = true)
  public ResponseEntity<List<ResponseReservation>> listReservations(@PathVariable Long roomId, @RequestParam LocalDate date) {
    return ResponseEntity.ok(reservationService.getRoomReservations(roomId, date));
  }
}
