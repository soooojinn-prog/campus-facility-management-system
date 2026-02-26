package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.BaseDevLoader;
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
@Component
@Profile("dev")
@Slf4j
@Order(2)
public class RoomDevLoader extends BaseDevLoader<Room> {
  private final BuildingRepository buildingRepo;
  private final RoomRepository roomRepo;

  public RoomDevLoader(ResourceLoader loader, ObjectMapper mapper, RoomRepository roomRepo, BuildingRepository buildingRepo) {
    super(loader, mapper, Room.class, "data/dev/rooms.jsonc");
    this.roomRepo = roomRepo;
    this.buildingRepo = buildingRepo;
  }

  @Override
  public void load() {

  }

  @Override
  public void unload() {

  }
}
