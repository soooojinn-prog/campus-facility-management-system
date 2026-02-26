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
    processItems(counselor -> {
      if (!repo.existsByName(counselor.getName())) {
        repo.save(counselor);
        log.info("Loaded dev counselor: ({} / {} / {})", counselor.getName(), counselor.getDepartment(), counselor.getPosition());
      }
    });
  }

  @Override
  public void unload() {
    processItems(counselor -> repo.findByName(counselor.getName()).ifPresent(existing -> {
      repo.delete(existing);
      log.info("Unloaded dev counselor: ({} / {} / {})", existing.getName(), existing.getDepartment(), existing.getPosition());
    }));
  }
}
