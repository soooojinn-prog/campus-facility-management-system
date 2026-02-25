package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.Club;
import io.github.wizwix.cfms.model.enums.ClubStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Long> {
  List<Club> findByStatus(ClubStatus status);
}
