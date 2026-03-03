package io.github.wizwix.cfms.model.counseling;

import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.enums.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "cfms_counseling_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CounselingReservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 상담사
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "counselor_id", nullable = false)
  private Counselor counselor;
  /// 예약 생성 시각
  private LocalDateTime createdAt;
  /// 상담 날짜
  private LocalDate date;
  /// 상담 종료 시간
  private LocalTime endTime;
  /// 메모
  @Column(length = 1000)
  private String memo;
  /// 상담 시작 시간
  private LocalTime startTime;
  /// 예약 상태 (PENDING/APPROVED/REJECTED/CANCELLED)
  @Enumerated(EnumType.STRING)
  private ReservationStatus status;
  /// 상담 주제
  @Column(length = 500)
  private String topic;
  /// 예약한 사용자
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}
