package io.github.wizwix.cfms.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.wizwix.cfms.global.jackson.BuildingSlugDeserializer;
import io.github.wizwix.cfms.model.enums.RoomType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cfms_room", uniqueConstraints = {@UniqueConstraint(columnNames = {"building_id", "name"}),})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
  /// 방 ID: 순수 인덱싱용
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 방 번호 (e.g. '104호')
  private String name;
  /// 방이 속한 건물 (e.g. '강의동 1')
  @ManyToOne(fetch = FetchType.LAZY)
  @JsonDeserialize(using = BuildingSlugDeserializer.class)
  private Building building;
  /// 방의 수용 가능 인원
  private Integer capacity;
  /// 방이 속한 층 (e.g. '3' - 3층)
  private Integer floor;
  /// 방 유형
  @Enumerated(EnumType.STRING)
  private RoomType type;

  public String getFullName() {
    return (building != null ? building.getName() : "") + " " + name;
  }
}
