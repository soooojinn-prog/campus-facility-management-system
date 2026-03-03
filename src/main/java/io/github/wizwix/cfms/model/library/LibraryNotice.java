package io.github.wizwix.cfms.model.library;

import io.github.wizwix.cfms.model.enums.LibraryNoticeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "cfms_library_notice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LibraryNotice {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(columnDefinition = "TEXT")
  private String content;
  private LocalDate date;
  private String title;
  @Enumerated(EnumType.STRING)
  private LibraryNoticeType type;
  private int views;
}
