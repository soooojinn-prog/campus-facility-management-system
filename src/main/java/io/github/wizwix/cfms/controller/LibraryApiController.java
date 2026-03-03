package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.dto.response.building.ResponseLibraryBook;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryCongestion;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryNotice;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryReadingRoom;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryStudyRoom;
import io.github.wizwix.cfms.service.iface.ILibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/// 도서관 전용 API 컨트롤러
/// Base: /api/library
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryApiController {
  private final ILibraryService libraryService;

  @GetMapping("/congestion")
  public ResponseEntity<ResponseLibraryCongestion> getCongestion() {
    return ResponseEntity.ok(libraryService.getCongestion());
  }

  @GetMapping("/notices/{noticeId}")
  public ResponseEntity<ResponseLibraryNotice> getNotice(@PathVariable Long noticeId) {
    return ResponseEntity.ok(libraryService.getNotice(noticeId));
  }

  @GetMapping("/notices")
  public ResponseEntity<List<ResponseLibraryNotice>> getNotices() {
    return ResponseEntity.ok(libraryService.getNotices());
  }

  @GetMapping("/reading-rooms/{roomId}/seats")
  public ResponseEntity<ResponseLibraryReadingRoom> getReadingRoomSeats(@PathVariable Long roomId) {
    return ResponseEntity.ok(libraryService.getReadingRoomSeats(roomId));
  }

  @GetMapping("/reading-rooms")
  public ResponseEntity<List<ResponseLibraryReadingRoom>> getReadingRooms() {
    return ResponseEntity.ok(libraryService.getReadingRooms());
  }

  @GetMapping("/study-rooms/{roomId}/slots")
  public ResponseEntity<ResponseLibraryStudyRoom> getStudyRoomSlots(@PathVariable Long roomId, @RequestParam String date) {
    return ResponseEntity.ok(libraryService.getStudyRoomSlots(roomId, date));
  }

  @GetMapping("/study-rooms")
  public ResponseEntity<List<ResponseLibraryStudyRoom>> getStudyRooms() {
    return ResponseEntity.ok(libraryService.getStudyRooms());
  }

  /// 내 열람실 좌석 예약 내역 — 로그인 필수
  @GetMapping("/reading-rooms/reservations/me")
  public ResponseEntity<List<Map<String, Object>>> mySeatReservations(Authentication auth) {
    return ResponseEntity.ok(libraryService.getMySeatReservations(auth.getName()));
  }

  /// 내 스터디룸 예약 내역 — 로그인 필수
  @GetMapping("/study-rooms/reservations/me")
  public ResponseEntity<List<Map<String, Object>>> myStudyRoomReservations(Authentication auth) {
    return ResponseEntity.ok(libraryService.getMyStudyRoomReservations(auth.getName()));
  }

  /// 도서 예약 — 로그인 필수
  @PostMapping("/books/{bookId}/reservation")
  public ResponseEntity<Void> reserveBook(@PathVariable Long bookId, Authentication auth) {
    libraryService.reserveBook(bookId, auth.getName());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /// 좌석 예약 — 로그인 필수
  @PostMapping("/reading-rooms/{roomId}/seats/{seatNo}/reservations")
  public ResponseEntity<Void> reserveSeat(@PathVariable Long roomId, @PathVariable Integer seatNo, Authentication auth) {
    libraryService.reserveSeat(roomId, seatNo, auth.getName());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /// 스터디룸 예약 — 로그인 필수
  /// Body: { "date": "2026-02-26", "startHour": 14 }
  @PostMapping("/study-rooms/{roomId}/reservations")
  public ResponseEntity<Void> reserveStudyRoom(@PathVariable Long roomId, @RequestBody Map<String, Object> body, Authentication auth) {
    String date = body.get("date").toString();
    Integer startHour = Integer.valueOf(body.get("startHour").toString());
    libraryService.reserveStudyRoom(roomId, date, startHour, auth.getName());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/books")
  public ResponseEntity<List<ResponseLibraryBook>> searchBooks(@RequestParam(required = false, defaultValue = "") String q, @RequestParam(required = false) String publisher, @RequestParam(required = false) String category) {
    return ResponseEntity.ok(libraryService.searchBooks(q, publisher, category));
  }
}
