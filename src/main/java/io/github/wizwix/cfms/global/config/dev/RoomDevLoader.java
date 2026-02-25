package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.DevDataLoader;
import io.github.wizwix.cfms.model.Building;
import io.github.wizwix.cfms.model.Room;
import io.github.wizwix.cfms.model.enums.RoomType;
import io.github.wizwix.cfms.repo.BuildingRepository;
import io.github.wizwix.cfms.repo.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Optional;

/// 호실 시드 데이터 로더 — rooms.jsonc → DB
/// BaseDevLoader를 사용하지 않는 이유:
///   rooms.jsonc의 "building" 필드가 Building 엔티티가 아닌 slug(String)이므로
///   Jackson 자동 역직렬화 불가 → JsonNode로 수동 파싱
///
/// @Order(2): BuildingDevLoader(@ Order ( 1)) 이후 실행 보장
///   DevDataOrchestrator가 List<DevDataLoader>를 순회할 때 @Order 값 기준 정렬됨
@Component("roomDevLoader")
@Profile("dev")
@Order(2)
@Slf4j
public class RoomDevLoader implements DevDataLoader {
  private final BuildingRepository buildingRepo;
  private final ObjectMapper mapper;
  private final ResourceLoader resourceLoader;
  private final RoomRepository roomRepo;

  public RoomDevLoader(ResourceLoader resourceLoader, ObjectMapper mapper, BuildingRepository buildingRepo, RoomRepository roomRepo) {
    this.resourceLoader = resourceLoader;
    this.buildingRepo = buildingRepo;
    this.roomRepo = roomRepo;
    this.mapper = mapper;
  }

  @Override
  public void load() {
    try {
      Resource resource = resourceLoader.getResource("classpath:data/dev/rooms.jsonc");
      if (!resource.exists()) {
        log.warn("rooms.jsonc not found");
        return;
      }
      try (InputStream is = resource.getInputStream()) {
        JsonNode nodes = mapper.readTree(is);
        for (JsonNode node : nodes) {
          String buildingSlug = node.get("building").asText();
          String roomNumber = node.get("roomNumber").asText();
          int capacity = node.get("capacity").asInt();
          int floor = node.get("floor").asInt();
          RoomType type = RoomType.valueOf(node.get("type").asText());

          Optional<Building> buildingOpt = buildingRepo.findBySlug(buildingSlug);
          if (buildingOpt.isEmpty()) {
            log.warn("Building not found: {}", buildingSlug);
            continue;
          }
          Building building = buildingOpt.get();
          if (roomRepo.existsByBuildingAndName(building, roomNumber)) {
            continue;
          }
          Room room = new Room();
          room.setName(roomNumber);
          room.setBuilding(building);
          room.setCapacity(capacity);
          room.setFloor(floor);
          room.setType(type);
          roomRepo.save(room);
          log.info("Loaded dev room: {} - {}", buildingSlug, roomNumber);
        }
      }
    } catch (Exception e) {
      log.error("Error loading rooms", e);
    }
  }

  @Override
  public void unload() {
    try {
      Resource resource = resourceLoader.getResource("classpath:data/dev/rooms.jsonc");
      if (!resource.exists()) return;
      try (InputStream is = resource.getInputStream()) {
        JsonNode nodes = mapper.readTree(is);
        for (JsonNode node : nodes) {
          String buildingSlug = node.get("building").asText();
          String roomNumber = node.get("roomNumber").asText();
          buildingRepo.findBySlug(buildingSlug).ifPresent(building ->
              roomRepo.getRoomsByBuildingSlug(building.getSlug()).stream()
                  .filter(r -> r.getName().equals(roomNumber))
                  .findFirst()
                  .ifPresent(room -> {
                    roomRepo.delete(room);
                    log.info("Unloaded dev room: {} - {}", buildingSlug, roomNumber);
                  })
          );
        }
      }
    } catch (Exception e) {
      log.error("Error unloading rooms", e);
    }
  }
}
