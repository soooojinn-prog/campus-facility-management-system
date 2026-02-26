package io.github.wizwix.cfms.dto.response.building;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LibraryNoticeResponse {
  private Long id;
  private String type;     // "공지" | "긴급" | "이벤트" | "안내"
  private String title;
  private String date;
  private int views;
  /** 상세 조회 시에만 채워짐 (목록 조회 시 null) */
  private String content;
}
