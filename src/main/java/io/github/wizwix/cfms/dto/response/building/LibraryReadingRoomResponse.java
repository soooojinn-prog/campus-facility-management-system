package io.github.wizwix.cfms.dto.response.building;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class LibraryReadingRoomResponse {
  private Long id;
  private String name;
  private String floor;
  private int totalSeats;
  private int usedSeats;
  /** 좌석 배치도 요청 시에만 채워짐 (목록 조회 시 null) */
  private List<Map<String, Object>> seats;
}
