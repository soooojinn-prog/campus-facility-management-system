package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.BaseDevLoader;
import io.github.wizwix.cfms.model.library.LibraryNotice;
import io.github.wizwix.cfms.repo.library.LibraryNoticeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

@Component
@Profile("dev")
@Slf4j
public class LibraryNoticeDevLoader extends BaseDevLoader<LibraryNotice> {
  private final LibraryNoticeRepository repo;

  public LibraryNoticeDevLoader(ResourceLoader loader, ObjectMapper mapper, LibraryNoticeRepository repo) {
    super(loader, mapper, LibraryNotice.class, "data/dev/library-notice.jsonc");
    this.repo = repo;
  }

  @Override
  public void load() {
    LongAdder adder = new LongAdder();
    processItems(notice -> {
      if (!repo.existsByTitleAndDate(notice.getTitle(), notice.getDate())) {
        repo.save(notice);
        adder.increment();
      }
    });
    log.info("Dev Profile: Loaded {} library notices", adder.sum());
  }

  @Override
  public void unload() {
    LongAdder adder = new LongAdder();
    processItems(notice -> repo.findByTitleAndDate(notice.getTitle(), notice.getDate()).ifPresent(existing -> {
      repo.delete(existing);
      adder.increment();
    }));
    log.info("Dev Profile: Unloaded {} library notices", adder.sum());
  }
}
