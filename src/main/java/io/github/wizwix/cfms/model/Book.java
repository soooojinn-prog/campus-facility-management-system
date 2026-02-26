package io.github.wizwix.cfms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cfms_book")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, length = 100)
  private String author;

  @Column(length = 100)
  private String publisher;

  @Column(length = 50)
  private String category;

  // 대출 가능 여부(샘플)
  @Column(nullable = false)
  private Boolean available = true;

  // 인기 정렬용(조회수 or 대출수)
  @Column(nullable = false)
  private Integer viewCount = 0;
}
