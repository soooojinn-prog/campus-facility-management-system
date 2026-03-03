package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.BaseDevLoader;
import io.github.wizwix.cfms.model.dorm.DormRoom;
import io.github.wizwix.cfms.repo.dorm.DormRoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

@Component
@Profile("dev")
@Slf4j
public class DormDevLoader extends BaseDevLoader<DormRoom> {
  private final DormRoomRepository repo;

  public DormDevLoader(ResourceLoader loader, ObjectMapper mapper, DormRoomRepository repo) {
    super(loader, mapper, DormRoom.class, "data/dev/dorm-rooms.jsonc");
    this.repo = repo;
  }

  @Override
  public void load() {
    LongAdder adder = new LongAdder();
    processItems(room -> {
      if (!repo.existsByRoomNumber(room.getRoomNumber())) {
        repo.save(room);
        adder.increment();
      }
    });
    log.info("Dev Profile: Loaded {} dorms", adder.sum());
  }

  @Override
  public void unload() {
    LongAdder adder = new LongAdder();
    processItems(room -> repo.findByRoomNumber(room.getRoomNumber()).ifPresent(existing -> {
      repo.delete(existing);
      adder.increment();
    }));
    log.info("Dev Profile: Unloaded {} dorms", adder.sum());
  }
}
