package io.github.wizwix.cfms.global.config.dev.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.wizwix.cfms.global.config.dev.DevDataLoader;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class UserDevLoader implements DevDataLoader {
  private final PasswordEncoder encoder;
  private final ResourceLoader loader;
  private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
  private final UserRepository repo;

  @Override
  public void load() {
    try {
      Resource resource = loader.getResource("classpath:data/dev/users.json");
      InputStream inputStream = resource.getInputStream();

      List<User> users = mapper.readValue(inputStream, new TypeReference<>() {});
      users.forEach(user -> {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(true);

        if (!repo.existsByEmail(user.getEmail())) {
          repo.save(user);
          log.info("Dev User created: {}", user.getEmail());
        }
      });
    } catch (Exception e) {
      log.error("Failed to load dev user data: {}", e.getMessage());
    }
  }

  @Override
  public void unload() {
    try {
      Resource resource = loader.getResource("classpath:data/dev/users.json");
      InputStream inputStream = resource.getInputStream();

      List<User> users = mapper.readValue(inputStream, new TypeReference<>() {});
      users.forEach(user -> {
        repo.findByEmail(user.getEmail()).ifPresent(u -> {
          repo.delete(u);
          log.info("Dev User deleted: {}", user.getEmail());
        });
      });
    } catch (Exception e) {
      log.error("Failed to unload dev user data: {}", e.getMessage());
    }
  }
}
