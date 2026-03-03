package io.github.wizwix.cfms.model.dorm;

import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.enums.DormApplicationStatus;
import io.github.wizwix.cfms.model.enums.DormPeriod;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "cfms_dorm_application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DormApplication {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 신청자
  @ManyToOne
  @JoinColumn(name = "applicant_id", nullable = false)
  private User applicant;
  /// 신청일시
  private LocalDateTime createdAt;
  /// 같이 신청한 친구 (null이면 단독 신청)
  @ManyToOne
  @JoinColumn(name = "partner_id")
  private User partner;
  /// 입주 기간
  @Enumerated(EnumType.STRING)
  private DormPeriod period;
  /// 신청 호실
  @ManyToOne
  @JoinColumn(name = "room_id", nullable = false)
  private DormRoom room;
  /// 학기 (e.g. "2026-1")
  private String semester;
  /// 신청 상태
  @Enumerated(EnumType.STRING)
  private DormApplicationStatus status;
}
