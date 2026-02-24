package io.github.wizwix.cfms.model;

import io.github.wizwix.cfms.model.enums.RoomStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(
    name = "cfms_room",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"building_id", "room_number"}),
        @UniqueConstraint(columnNames = {"room_code"}),
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
  /// 호실 ID: 순수 인덱싱용
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /// 호실이 속한 건물
  @ManyToOne(fetch = FetchType.LAZY)
  private Building building;
  /// 수용 가능 인원
  private Integer capacity;
  /// 호실의 정식 명칭 (e.g. 'IT대학동 104호')
  @Column(unique = true, nullable = false)
  private String roomCode;
  /// 호실의 방 번호 (e.g. '104')
  private String roomNumber;
  /// 호실 상태
  // TODO: 이 필드가 정말 필요할까? 백엔드에서 Reservation 테이블을 검색해 동적으로 지정할 수는 없는 걸까?
  private RoomStatus status;
}
