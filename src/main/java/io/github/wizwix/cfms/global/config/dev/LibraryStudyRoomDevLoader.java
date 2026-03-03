package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.BaseDevLoader;
import io.github.wizwix.cfms.model.library.LibraryStudyRoom;
import io.github.wizwix.cfms.repo.library.LibraryStudyRoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

@Component
@Profile("dev")
@Slf4j
public class LibraryStudyRoomDevLoader extends BaseDevLoader<LibraryStudyRoom> {
  private final LibraryStudyRoomRepository repo;

  public LibraryStudyRoomDevLoader(ResourceLoader loader, ObjectMapper mapper, LibraryStudyRoomRepository repo) {
    super(loader, mapper, LibraryStudyRoom.class, "data/dev/library-study-rooms.jsonc");
    this.repo = repo;
  }

  @Override
  public void load() {
    LongAdder adder = new LongAdder();
    processItems(room -> {
      if (!repo.existsByName(room.getName())) {
        repo.save(room);
        adder.increment();
      }
    });
    log.info("Dev Profile: Loaded {} library study rooms", adder.sum());
  }

  @Override
  public void unload() {
    LongAdder adder = new LongAdder();
    processItems(room -> repo.findByName(room.getName()).ifPresent(existing -> {
      repo.delete(existing);
      adder.increment();
    }));
    log.info("Dev Profile: Unloaded {} library study rooms", adder.sum());
  }
}
