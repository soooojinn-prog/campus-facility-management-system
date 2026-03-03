package io.github.wizwix.cfms.model.library;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cfms_library_book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryBook {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(nullable = false, length = 100)
  private String author;
  /// 대출 가능 여부(샘플)
  @Column(nullable = false)
  private Boolean available = true;
  @Column(length = 50)
  private String category;
  @Column(length = 100)
  private String location;
  @Column(length = 100)
  private String publisher;
  @Column(nullable = false, length = 200)
  private String title;
  /// 인기 정렬용(조회수 or 대출수)
  @Column(nullable = false)
  private Integer viewCount = 0;
  @Column(nullable = false)
  private Integer year = 0;
}
