package io.github.wizwix.cfms.model.library;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/// 스터디룸 예약
/// - roomId: 스터디룸 ID
/// - date: 예약 날짜
/// - startHour: 시작 시간 (9~18 정수)
/// - userNumber: 예약자 학번/교번
@Entity
@Table(name = "cfms_library_studyroom_reservation", uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "date", "start_hour"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryStudyRoomReservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  private LocalDate date;
  @Column(name = "room_id", nullable = false)
  private Long roomId;
  @Column(name = "start_hour", nullable = false)
  private Integer startHour;
  @Column(name = "user_number", nullable = false, length = 50)
  private String userNumber;
}
