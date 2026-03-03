package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.wizwix.cfms.global.config.dev.base.BaseDevLoader;
import io.github.wizwix.cfms.global.config.dev.base.EntityReferenceDeserializer;
import io.github.wizwix.cfms.model.Building;
import io.github.wizwix.cfms.model.Room;
import io.github.wizwix.cfms.repo.BuildingRepository;
import io.github.wizwix.cfms.repo.RoomRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

/// 호실 시드 데이터 로더 — rooms.jsonc → DB
/// BaseDevLoader를 사용하지 않는 이유:
///   rooms.jsonc의 "building" 필드가 Building 엔티티가 아닌 slug(String)이므로
///   Jackson 자동 역직렬화 불가 → JsonNode로 수동 파싱
///
/// @Order(2): BuildingDevLoader(@ Order ( 1)) 이후 실행 보장
///   DevDataOrchestrator가 List<DevDataLoader>를 순회할 때 @Order 값 기준 정렬됨
@Component
@Profile("dev")
@Slf4j
@Order(3)
public class RoomDevLoader extends BaseDevLoader<Room> {
  private final BuildingRepository buildingRepo;
  private final RoomRepository roomRepo;

  public RoomDevLoader(ResourceLoader loader, ObjectMapper mapper, RoomRepository roomRepo, BuildingRepository buildingRepo) {
    super(loader, mapper, Room.class, "data/dev/rooms.jsonc");
    this.roomRepo = roomRepo;
    this.buildingRepo = buildingRepo;
  }

  @Override
  protected void configureMapper(ObjectMapper mapper) {
    SimpleModule module = new SimpleModule();
    // String -> Building 변환
    module.addDeserializer(Building.class, new EntityReferenceDeserializer<>(building -> buildingRepo.findBySlug(building).orElseThrow(() -> new RuntimeException("Building[" + building + "] not found"))));
    mapper.registerModule(module);
  }

  @Override
  public void load() {
    LongAdder adder = new LongAdder();
    processItems(room -> {
      if (!roomRepo.existsByFullName(room.getFullName())) {
        roomRepo.save(room);
        adder.increment();
      }
    });
    log.info("Dev Profile: Loaded {} rooms", adder.sum());
  }

  @Override
  public void unload() {
    LongAdder adder = new LongAdder();
    processItems(room -> roomRepo.findByFullName(room.getFullName()).ifPresent(existing -> {
      roomRepo.delete(existing);
      adder.increment();
    }));
    log.info("Dev Profile: Unloaded {} rooms", adder.sum());
  }
}
