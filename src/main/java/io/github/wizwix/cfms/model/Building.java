package io.github.wizwix.cfms.model;

import io.github.wizwix.cfms.model.enums.BuildingType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cfms_building")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Building {
  /// 건물 ID: 순수 인덱싱용
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 건물 이름 (e.g. '강의동 1')
  private String name;
  /// 건물 설명 (e.g. '강의실 XX개, 1~X층')
  private String info;
  /// 캠퍼스 전체 지도에서 해당 건물이 차지하는 영역의 각 꼭지점 모음
  private String points;
  /// 건물의 대여 가능한 방 존재 여부
  private Boolean rentable;
  /// 건물 유형
  private BuildingType type;
}
