package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByNumber(String number);

  Optional<User> findByEmail(String email);

  Optional<User> findByNumber(String userNumber);

  Optional<User> findByNumberAndEmailAndEnabledTrue(String userNumber, String email);

  Optional<User> findByNumberAndEnabledTrue(String userNumber);
}
