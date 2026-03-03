package io.github.wizwix.cfms.repo.library;

import io.github.wizwix.cfms.model.library.LibraryBookReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryBookReservationRepository extends JpaRepository<LibraryBookReservation, Long> {
  boolean existsByBookIdAndUserNumber(Long bookId, String userNumber);

  List<LibraryBookReservation> findByUserNumber(String userNumber);
}
