package io.github.wizwix.cfms.repo.library;

import io.github.wizwix.cfms.model.library.LibrarySeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LibrarySeatReservationRepository extends JpaRepository<LibrarySeatReservation, Long> {
  // 특정 좌석 중복 예약 확인
  boolean existsByRoomIdAndSeatNoAndDate(Long roomId, Integer seatNo, LocalDate date);

  // 한 유저가 오늘 어떤 열람실이든 이미 예약했는지 확인 (1인 1좌석 제한)
  boolean existsByUserNumberAndDate(String userNumber, LocalDate date);

  // 특정 열람실 + 날짜의 예약된 좌석 목록
  List<LibrarySeatReservation> findByRoomIdAndDate(Long roomId, LocalDate date);

  // 유저의 전체 예약 내역 조회 (마이페이지용)
  List<LibrarySeatReservation> findByUserNumber(String userNumber);

  // 유저의 오늘 예약 정보 조회 (내 예약 확인용)
  List<LibrarySeatReservation> findByUserNumberAndDate(String userNumber, LocalDate date);
}
