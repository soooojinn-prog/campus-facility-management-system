package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.BaseDevLoader;
import io.github.wizwix.cfms.model.library.LibraryBook;
import io.github.wizwix.cfms.repo.library.LibraryBookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

@Component
@Profile("dev")
@Slf4j
public class BookDevLoader extends BaseDevLoader<LibraryBook> {
  private final LibraryBookRepository repo;

  public BookDevLoader(ResourceLoader loader, ObjectMapper mapper, LibraryBookRepository repo) {
    super(loader, mapper, LibraryBook.class, "data/dev/books.jsonc");
    this.repo = repo;
  }

  @Override
  public void load() {
    LongAdder adder = new LongAdder();
    processItems(book -> {
      if (!repo.existsByTitleAndAuthor(book.getTitle(), book.getAuthor())) {
        repo.save(book);
        adder.increment();
      }
    });
    log.info("Dev Profile: Loaded {} books", adder.sum());
  }

  @Override
  public void unload() {
    LongAdder adder = new LongAdder();
    processItems(book -> repo.findByTitleAndAuthor(book.getTitle(), book.getAuthor()).ifPresent(existing -> {
      repo.delete(existing);
      adder.increment();
    }));
    log.info("Dev Profile: Unloaded {} books", adder.sum());
  }
}
