package io.github.wizwix.cfms.model;

import io.github.wizwix.cfms.model.enums.Gender;
import io.github.wizwix.cfms.model.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cfms_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
  /// 회원 ID: 순수 인덱싱용
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 회원 이름
  private String name;
  /// 비밀번호
  private String password; // BCrypt로 암호화된 비밀번호
  /// 이메일
  @Column(unique = true)
  private String email;
  /// 회원 생성 시각
  private LocalDateTime createdAt;
  /// 계정 탈퇴 시각 - 탈퇴 30일 후 해당 개체 삭제
  private LocalDateTime disabledAt;
  /// 회원의 탈퇴 여부: Table에서 해당 줄을 즉시 지우는 대신, 해당 Field를 `false`로 설정해 Soft Delete 구현
  private Boolean enabled = true;
  /// 성별
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, columnDefinition = "varchar(50)")
  private Gender gender;
  /// 학번 or 교번 (e.g. '260004181')
  @Column(unique = true, nullable = false)
  private String number;
  /// 비밀번호 초기화 토큰
  @Column(length = 6)
  private String passwordResetToken;
  /// 비밀번호 초기화 토큰 만료시각
  private LocalDateTime passwordResetTokenExpiry;
  /// 회원의 유형 (학생, 교수, 관리자)
  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private UserRole role;
}
