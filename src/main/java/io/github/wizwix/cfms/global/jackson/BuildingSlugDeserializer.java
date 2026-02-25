package io.github.wizwix.cfms.global.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.github.wizwix.cfms.model.Building;
import io.github.wizwix.cfms.repo.BuildingRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

/// Room.building 필드의 JSONC 역직렬화 시 slug(String) → Building 엔티티 변환
/// Spring Boot의 ObjectMapper가 SpringHandlerInstantiator를 사용하므로
/// @Component로 등록하면 Jackson이 자동으로 Spring 빈을 사용함
@Component
public class BuildingSlugDeserializer extends JsonDeserializer<Building> {
  private final BuildingRepository buildingRepository;

  public BuildingSlugDeserializer(BuildingRepository buildingRepository) {
    this.buildingRepository = buildingRepository;
  }

  @Override
  public Building deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    String slug = p.getValueAsString();
    return buildingRepository.findBySlug(slug).orElse(null);
  }
}
