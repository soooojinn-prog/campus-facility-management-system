package io.github.wizwix.cfms.model.club;

import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.enums.ClubMemberStatus;
import io.github.wizwix.cfms.model.enums.ClubRole;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "cfms_club_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClubMember {
  /// 동아리 회원 ID: 순수 인덱싱용
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 동아리
  @ManyToOne
  private Club club;
  /// 동아리 참여 일자
  private LocalDateTime joinedAt;
  /// (가입 거절 시) 거절 사유
  private String rejectReason;
  /// 동아리 부원 역할 (부원, 부장, 부부장)
  @Enumerated(EnumType.STRING)
  private ClubRole role;
  /// 동아리 가입 상태 (가입 승인됨, 탈퇴함, 가입 승인 대기 중, 가입 거부 됨)
  // 가입 거부된 줄은 매일 오전 0시에 삭제 처리되어야 함
  @Enumerated(EnumType.STRING)
  private ClubMemberStatus status;
  /// 회원
  @ManyToOne
  private User user;
}
