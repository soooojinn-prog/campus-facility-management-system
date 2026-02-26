package io.github.wizwix.cfms.dto.response.building;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class LibraryStudyRoomResponse {
  private Long id;
  private String name;
  private String floor;
  private int capacity;
  private List<String> amenities;
  /** AVAILABLE / OCCUPIED / MAINTENANCE */
  private String status;
  /** 슬롯 조회 시에만 채워짐 (목록 조회 시 null) */
  private List<String> occupiedSlots;
}
