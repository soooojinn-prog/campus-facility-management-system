package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.StudyRoomReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface StudyRoomReservationRepository extends JpaRepository<StudyRoomReservation, Long> {

  // 특정 스터디룸 + 날짜의 예약된 시간 목록
  List<StudyRoomReservation> findByRoomIdAndDate(Long roomId, LocalDate date);

  // 특정 방 + 날짜 + 시간 중복 확인
  boolean existsByRoomIdAndDateAndStartHour(Long roomId, LocalDate date, Integer startHour);

  // 같은 유저가 같은 날 같은 시간에 어느 방이든 이미 예약했는지 확인 (시간 중복 방지)
  boolean existsByUserNumberAndDateAndStartHour(String userNumber, LocalDate date, Integer startHour);

  // 유저의 예약 목록 조회 (내 예약 확인용)
  List<StudyRoomReservation> findByUserNumberAndDate(String userNumber, LocalDate date);
}
