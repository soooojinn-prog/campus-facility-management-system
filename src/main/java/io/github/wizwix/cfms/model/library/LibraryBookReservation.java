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

import java.time.LocalDateTime;

/// 도서 예약
/// - bookId: 예약할 도서 ID
/// - userNumber: 예약자 학번/교번
/// - reservedAt: 예약 시각
@Entity
@Table(name = "cfms_library_book_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryBookReservation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "book_id", nullable = false)
  private Long bookId;
  @Column(name = "reserved_at", nullable = false)
  private LocalDateTime reservedAt;
  @Column(name = "user_number", nullable = false, length = 50)
  private String userNumber;
}
