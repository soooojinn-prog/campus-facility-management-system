package io.github.wizwix.cfms.global.config.dev.base;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public abstract class BaseDevLoader<T> implements DevDataLoader {
  protected final ResourceLoader loader;
  protected final ObjectMapper mapper;
  private final Class<T> entityClass;
  private final String jsonPath;

  protected BaseDevLoader(ResourceLoader loader, Class<T> entityClass, String jsonPath) {
    this.loader = loader;
    this.entityClass = entityClass;
    this.jsonPath = jsonPath;
    this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
  }

  protected void processItems(Consumer<T> action) {
    List<T> items = readJsonData();
    items.forEach(action);
  }

  protected List<T> readJsonData() {
    try {
      Resource resource = loader.getResource("classpath:" + jsonPath);
      if (!resource.exists()) {
        log.warn("Resource not found: {}", jsonPath);
        return new ArrayList<>();
      }
      try (InputStream is = resource.getInputStream()) {
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, entityClass);
        return mapper.readValue(is, type);
      }
    } catch (Exception e) {
      log.error("Error reading JSON from {}", jsonPath, e);
      return new ArrayList<>();
    }
  }
}
