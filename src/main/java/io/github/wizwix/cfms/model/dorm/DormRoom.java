package io.github.wizwix.cfms.model.dorm;

import io.github.wizwix.cfms.model.enums.Gender;
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

@Entity
@Table(name = "cfms_dorm_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DormRoom {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 층 (1~5)
  private Integer floor;
  /// 남자/여자 기숙사
  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private Gender gender;
  /// 호실 번호 (e.g. "M101", "F203")
  private String roomNumber;
}
