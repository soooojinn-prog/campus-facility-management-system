package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.BaseDevLoader;
import io.github.wizwix.cfms.model.counseling.Counselor;
import io.github.wizwix.cfms.repo.counceling.CounselorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

@Component
@Profile("dev")
@Slf4j
public class CounselorDevLoader extends BaseDevLoader<Counselor> {
  private final CounselorRepository repo;

  public CounselorDevLoader(ResourceLoader loader, ObjectMapper mapper, CounselorRepository repo) {
    super(loader, mapper, Counselor.class, "data/dev/counselors.jsonc");
    this.repo = repo;
  }

  @Override
  public void load() {
    LongAdder adder = new LongAdder();
    processItems(counselor -> {
      if (!repo.existsByName(counselor.getName())) {
        repo.save(counselor);
        adder.increment();
      }
    });
    log.info("Dev Profile: Loaded {} counselors", adder.sum());
  }

  @Override
  public void unload() {
    LongAdder adder = new LongAdder();
    processItems(counselor -> repo.findByName(counselor.getName()).ifPresent(existing -> {
      repo.delete(existing);
      adder.increment();
    }));
    log.info("Dev Profile: Unloaded {} counselors", adder.sum());
  }
}
