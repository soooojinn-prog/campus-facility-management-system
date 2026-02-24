package io.github.wizwix.cfms.global.config.dev;

import io.github.wizwix.cfms.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class UserDevLoader implements DevDataLoader {
  private final UserRepository repo;
  private final PasswordEncoder encoder;
  private final ResourceLoader loader;

  @Override
  public void load() {

  }

  @Override
  public void unload() {

  }
}
