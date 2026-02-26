package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.BaseDevLoader;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("dev")
@Slf4j
public class UserDevLoader extends BaseDevLoader<User> {
  private final PasswordEncoder encoder;
  private final UserRepository repo;

  public UserDevLoader(ResourceLoader loader, ObjectMapper mapper, PasswordEncoder encoder, UserRepository repo) {
    super(loader, mapper, User.class, "data/dev/users.jsonc");
    this.encoder = encoder;
    this.repo = repo;
  }

  @Override
  public void load() {
    processItems(user -> {
      if (!repo.existsByNumber(user.getNumber())) {
        String rawPassword = user.getPassword();
        user.setPassword(encoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setEnabled(true);

        repo.save(user);
        log.info("Loaded dev user: ({} / {} / {})", user.getNumber(), rawPassword, user.getRole());
      }
    });
  }

  @Override
  public void unload() {
    processItems(user -> repo.findByNumber(user.getNumber()).ifPresent(existing -> {
      repo.delete(existing);
      log.info("Unloaded dev user: ({} / {} / {})", existing.getNumber(), existing.getName(), existing.getRole());
    }));
  }
}
