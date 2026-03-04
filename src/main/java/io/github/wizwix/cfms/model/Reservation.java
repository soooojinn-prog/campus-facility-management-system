package io.github.wizwix.cfms.model;

import io.github.wizwix.cfms.model.club.Club;
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
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Entity
@Table(name = "cfms_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
  /// 시설 예약 ID
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 동아리 예약일 경우, 해당 동아리
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "club_id")
  private @Nullable Club club;
  /// 시설 예약 신청 시각
  private LocalDateTime createdAt;
  /// 시설 이용 종료 시각
  private LocalDateTime endTime;
  /// 예약 처리한 관리자
  @ManyToOne
  private User processedBy;
  /// 시설 예약 목적
  @Column(length = 1000)
  private String purpose;
  /// 예약 거절 사유 (관리자)
  private String rejectReason;
  /// 예약할 호실
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;
  /// 시설 이용 시작 시각
  private LocalDateTime startTime;
  /// 시설 예약 현황 (신청 허가됨, 신청 취소됨, 승인 대기 중, 승인 반려됨)
  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private ReservationStatus status;
  /// 시설 예약 갱신 시각
  private LocalDateTime updatedAt;
  /// 예약한 사람
  @ManyToOne(fetch = FetchType.LAZY)
  private User user;
}
