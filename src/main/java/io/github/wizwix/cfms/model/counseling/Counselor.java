package io.github.wizwix.cfms.model.counseling;

import io.github.wizwix.cfms.model.enums.CounselingDepartment;
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

@Entity
@Table(name = "cfms_counselor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Counselor {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 상담사 이름
  private String name;
  /// 소속 부서
  @Enumerated(EnumType.STRING)
  private CounselingDepartment department;
  /// 직위 (e.g. "교무처 상담관")
  private String position;
  /// 전문분야 (e.g. "수강신청 · 학점 관리 · 전과/복수전공")
  private String specialization;
}
