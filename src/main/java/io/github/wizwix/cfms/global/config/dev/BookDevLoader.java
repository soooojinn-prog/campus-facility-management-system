package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.BaseDevLoader;
import io.github.wizwix.cfms.model.Book;
import io.github.wizwix.cfms.repo.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@Slf4j
public class BookDevLoader extends BaseDevLoader<Book> {
  private final BookRepository repo;

  public BookDevLoader(ResourceLoader loader, ObjectMapper mapper, BookRepository repo) {
    super(loader, mapper, Book.class, "data/dev/books.jsonc");
    this.repo = repo;
  }

  @Override
  public void load() {
    processItems(book -> {
      if (!repo.existsByTitleAndAuthor(book.getTitle(), book.getAuthor())) {
        repo.save(book);
        log.info("Loaded dev book: ({} / {} / {})", book.getTitle(), book.getAuthor(), book.getPublisher());
      }
    });
  }

  @Override
  public void unload() {
    processItems(book -> repo.findByTitleAndAuthor(book.getTitle(), book.getAuthor()).ifPresent(existing -> {
      repo.delete(existing);
      log.info("Unloaded dev book: ({} / {} / {})", existing.getTitle(), existing.getAuthor(), existing.getPublisher());
    }));
  }
}
