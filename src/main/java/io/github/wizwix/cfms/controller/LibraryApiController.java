package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.dto.response.building.*;
import io.github.wizwix.cfms.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 도서관 전용 API 컨트롤러
 * Base: /api/buildings/{buildingId}/library
 */
@RestController
@RequestMapping("/api/buildings/{buildingId}/library")
@RequiredArgsConstructor
public class LibraryApiController {

  private final LibraryService libraryService;

  // ── 1. 열람실 ──
  @GetMapping("/reading-rooms")
  public ResponseEntity<List<ResponseLibraryReadingRoom>> getReadingRooms(
      @PathVariable Long buildingId) {
    return ResponseEntity.ok(libraryService.getReadingRooms(buildingId));
  }

  @GetMapping("/reading-rooms/{roomId}/seats")
  public ResponseEntity<ResponseLibraryReadingRoom> getReadingRoomSeats(
      @PathVariable Long buildingId,
      @PathVariable Long roomId) {
    return ResponseEntity.ok(libraryService.getReadingRoomSeats(buildingId, roomId));
  }

  /**
   * 좌석 예약 — 로그인 필수
   * POST /api/buildings/{buildingId}/library/reading-rooms/{roomId}/seats/{seatNo}/reserve
   */
  @PostMapping("/reading-rooms/{roomId}/seats/{seatNo}/reserve")
  public ResponseEntity<Void> reserveSeat(
      @PathVariable Long buildingId,
      @PathVariable Long roomId,
      @PathVariable Integer seatNo,
      Authentication auth) {
    // Authentication에서 학번 추출 (Spring Security Principal)
    String userNumber = auth.getName();
    libraryService.reserveSeat(buildingId, roomId, seatNo, userNumber);
    return ResponseEntity.ok().build();
  }

  // ── 2. 도서 검색 ──
  @GetMapping("/books")
  public ResponseEntity<List<ResponseLibraryBook>> searchBooks(
      @PathVariable Long buildingId,
      @RequestParam(required = false, defaultValue = "") String q,
      @RequestParam(required = false) String publisher,
      @RequestParam(required = false) String category) {
    return ResponseEntity.ok(libraryService.searchBooks(buildingId, q, publisher, category));
  }

  @PostMapping("/books/{bookId}/reserve")
  public ResponseEntity<Void> reserveBook(
      @PathVariable Long buildingId,
      @PathVariable Long bookId) {
    libraryService.reserveBook(buildingId, bookId);
    return ResponseEntity.ok().build();
  }

  // ── 3. 스터디룸 ──
  @GetMapping("/study-rooms")
  public ResponseEntity<List<ResponseLibraryStudyRoom>> getStudyRooms(
      @PathVariable Long buildingId) {
    return ResponseEntity.ok(libraryService.getStudyRooms(buildingId));
  }

  @GetMapping("/study-rooms/{roomId}/slots")
  public ResponseEntity<ResponseLibraryStudyRoom> getStudyRoomSlots(
      @PathVariable Long buildingId,
      @PathVariable Long roomId,
      @RequestParam String date) {
    return ResponseEntity.ok(libraryService.getStudyRoomSlots(buildingId, roomId, date));
  }

  /**
   * 스터디룸 예약 — 로그인 필수
   * POST /api/buildings/{buildingId}/library/study-rooms/{roomId}/reserve
   * Body: { "date": "2026-02-26", "startHour": 14 }
   */
  @PostMapping("/study-rooms/{roomId}/reserve")
  public ResponseEntity<Void> reserveStudyRoom(
      @PathVariable Long buildingId,
      @PathVariable Long roomId,
      @RequestBody Map<String, Object> body,
      Authentication auth) {
    String userNumber = auth.getName();
    String date       = body.get("date").toString();
    Integer startHour = Integer.valueOf(body.get("startHour").toString());
    libraryService.reserveStudyRoom(buildingId, roomId, date, startHour, userNumber);
    return ResponseEntity.ok().build();
  }

  // ── 4. 혼잡도 ──
  @GetMapping("/congestion")
  public ResponseEntity<ResponseLibraryCongestion> getCongestion(
      @PathVariable Long buildingId) {
    return ResponseEntity.ok(libraryService.getCongestion(buildingId));
  }

  // ── 5. 공지사항 ──
  @GetMapping("/notices")
  public ResponseEntity<List<ResponseLibraryNotice>> getNotices(
      @PathVariable Long buildingId) {
    return ResponseEntity.ok(libraryService.getNotices(buildingId));
  }

  @GetMapping("/notices/{noticeId}")
  public ResponseEntity<ResponseLibraryNotice> getNotice(
      @PathVariable Long buildingId,
      @PathVariable Long noticeId) {
    return ResponseEntity.ok(libraryService.getNotice(buildingId, noticeId));
  }

  // ── 마이페이지: 내 예약 조회 및 취소 ──

  @GetMapping("/reading-rooms/reservations/me")
  public ResponseEntity<List<Map<String, Object>>> mySeatReservations(
      @PathVariable Long buildingId, Authentication auth) {
    return ResponseEntity.ok(libraryService.getMySeatReservations(auth.getName()));
  }

  @DeleteMapping("/reading-rooms/reservations/{reservationId}")
  public ResponseEntity<Void> cancelSeatReservation(
      @PathVariable Long buildingId,
      @PathVariable Long reservationId,
      Authentication auth) {
    libraryService.cancelSeatReservation(auth.getName(), reservationId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/study-rooms/reservations/me")
  public ResponseEntity<List<Map<String, Object>>> myStudyRoomReservations(
      @PathVariable Long buildingId, Authentication auth) {
    return ResponseEntity.ok(libraryService.getMyStudyRoomReservations(auth.getName()));
  }

  @DeleteMapping("/study-rooms/reservations/{reservationId}")
  public ResponseEntity<Void> cancelStudyRoomReservation(
      @PathVariable Long buildingId,
      @PathVariable Long reservationId,
      Authentication auth) {
    libraryService.cancelStudyRoomReservation(auth.getName(), reservationId);
    return ResponseEntity.ok().build();
  }
}
