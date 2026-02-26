package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.BaseDevLoader;
import io.github.wizwix.cfms.model.Building;
import io.github.wizwix.cfms.repo.BuildingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

@Component("buildingDevLoader")
@Profile("dev")
@Order(1)
@Slf4j
public class BuildingDevLoader extends BaseDevLoader<Building> {
  private final BuildingRepository repo;

  public BuildingDevLoader(ResourceLoader loader, ObjectMapper mapper, BuildingRepository repo) {
    super(loader, mapper, Building.class, "data/dev/buildings.jsonc");
    this.repo = repo;
  }

  @Override
  public void load() {
    LongAdder adder = new LongAdder();
    processItems(building -> {
      if (!repo.existsBySlug(building.getSlug())) {
        repo.save(building);
        adder.increment();
      }
    });
    log.info("Dev Profile: Loaded {} buildings", adder.sum());
  }

  @Override
  public void unload() {
    LongAdder adder = new LongAdder();
    processItems(building -> repo.findBySlug(building.getSlug()).ifPresent(existing -> {
      repo.delete(existing);
      adder.increment();
    }));
    log.info("Dev Profile: Unloaded {} buildings", adder.sum());
  }
}
