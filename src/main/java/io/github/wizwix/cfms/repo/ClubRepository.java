package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.Club;
import io.github.wizwix.cfms.model.enums.ClubStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {
  boolean existsBySlug(String slug);

  Optional<Club> findBySlug(String slug);

  List<Club> findByStatus(ClubStatus status);
}
