package io.github.wizwix.cfms.repo.library;

import io.github.wizwix.cfms.model.library.LibraryBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {
  boolean existsByTitleAndAuthor(String title, String author);

  Optional<LibraryBook> findByTitleAndAuthor(String title, String author);
}
