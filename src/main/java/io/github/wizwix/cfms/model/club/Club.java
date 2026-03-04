package io.github.wizwix.cfms.model.club;

import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.enums.ClubStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cfms_club")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Club {
  /// 동아리 ID
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 동아리 이름
  private String name;
  /// 부원 자동 가입 허가 여부
  private Boolean autoApprove;
  /// 동아리 생성일자
  private LocalDateTime createdAt;
  /// 동아리 설명
  @Column(length = 5000)
  private String description;
  /// 동아리 회장
  @ManyToOne(fetch = FetchType.LAZY)
  private User president;
  /// 동아리 개설 거절 사유 (관리자)
  private String rejectReason;
  /// 동아리 URL 식별자
  @Column(unique = true, length = 10)
  private String slug;
  /// 동아리 상태 (개설 승인됨, 개설 승인 대기 중, 개설 승인 거부 됨)
  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private ClubStatus status;
}
