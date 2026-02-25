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
    processItems(building -> {
      if (!repo.existsBySlug(building.getSlug())) {
        repo.save(building);
        log.info("Loaded dev building: ({} / {} / {})", building.getName(), building.getSlug(), building.getInfo());
      }
    });
  }

  @Override
  public void unload() {
    processItems(building -> repo.findBySlug(building.getSlug()).ifPresent(existingBuilding -> {
      if (building.getName().equals(existingBuilding.getName()) && building.getInfo().equals(existingBuilding.getInfo())) {
        repo.delete(existingBuilding);
        log.info("Unloaded dev building: ({} / {} / {})", building.getName(), building.getSlug(), building.getInfo());
      }
    }));
  }
}
