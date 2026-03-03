package io.github.wizwix.cfms.repo.library;

import io.github.wizwix.cfms.model.library.LibraryNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface LibraryNoticeRepository extends JpaRepository<LibraryNotice, Long> {
  boolean existsByTitleAndDate(String title, LocalDate date);

  Optional<LibraryNotice> findByTitleAndDate(String title, LocalDate date);
}
