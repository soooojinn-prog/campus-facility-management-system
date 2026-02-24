package io.github.wizwix.cfms.global.config.dev;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataOrchestrator {
  private final List<DevDataLoader> loaders;

  @EventListener(ContextClosedEvent.class)
  public void onShutdown() {
    log.info("Dev Profile: Cleaning up sample data...");
    loaders.forEach(DevDataLoader::unload);
  }

  @EventListener(ContextRefreshedEvent.class)
  public void onStartup() {
    log.info("Dev Profile: Loading sample data...");
    loaders.forEach(DevDataLoader::load);
  }
}
