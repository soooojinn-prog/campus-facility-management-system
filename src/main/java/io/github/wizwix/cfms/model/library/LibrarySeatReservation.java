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

/// 열람실 좌석 예약
/// - roomId: 열람실 ID (1=제1열람실 2F, 2=제2열람실 3F, 3=야간 B1, 4=디지털 4F)
/// - seatNo: 좌석 번호
/// - date: 예약 날짜
/// - userNumber: 예약자 학번/교번
@Entity
@Table(name = "cfms_library_seat_reservation", uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "seat_no", "date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibrarySeatReservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false)
  private LocalDate date;
  @Column(name = "room_id", nullable = false)
  private Long roomId;
  @Column(name = "seat_no", nullable = false)
  private Integer seatNo;
  @Column(name = "user_number", nullable = false, length = 50)
  private String userNumber;
}
