package io.github.wizwix.cfms.dto.response.building;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseLibraryBook {
  private Long id;
  private String author;
  private boolean available;
  private String category;
  private String location;
  private String publisher;
  private String title;
  private int year;
}
