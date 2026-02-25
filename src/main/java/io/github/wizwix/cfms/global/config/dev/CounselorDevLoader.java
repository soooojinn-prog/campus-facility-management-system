package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.BaseDevLoader;
import io.github.wizwix.cfms.model.Counselor;
import io.github.wizwix.cfms.repo.CounselorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

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
    if (repo.count() > 0) return;
    int added = 0;
    processItems(counselor -> {
      if (!repo.existsByName(counselor.getName())) {
        repo.save(counselor);
        added++;
      }
    });
    log.info("Loaded dev counselors: {} counselors", added);
  }

  @Override
  public void unload() {
    int deleted = 0;
    processItems(counselor -> repo.findByName(counselor.getName()).ifPresent(existing -> {
      if (counselor.getDepartment() == existing.getDepartment() && counselor.getPosition().equals(existing.getPosition())) {
        repo.delete(existing);
        deleted++;
      }
    }));
    log.info("Unloaded dev counselors: {}", deleted);
  }
}
