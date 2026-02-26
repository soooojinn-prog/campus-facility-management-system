package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

  boolean existsByTitleAndAuthor(String title, String author);

  Optional<Book> findByTitleAndAuthor(String title, String author);
}
