package io.github.wizwix.cfms.dto.response.building;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LibraryBookResponse {
  private Long id;
  private String title;
  private String author;
  private String publisher;
  private String category;
  private boolean available;
  private String location;  // TODO: Book 모델에 location 필드 추가 후 연동 (현재 "미지정" 하드코딩)
  private int year;         // TODO: Book 모델에 year 필드 추가 후 연동 (현재 0 하드코딩)
}
