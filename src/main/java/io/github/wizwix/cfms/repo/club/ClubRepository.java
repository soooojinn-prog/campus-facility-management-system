package io.github.wizwix.cfms.repo.club;

import io.github.wizwix.cfms.model.club.Club;
import io.github.wizwix.cfms.model.enums.ClubStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Long> {
  boolean existsBySlug(String slug);

  Optional<Club> findBySlug(String slug);

  List<Club> findByStatus(ClubStatus status);

  @Query("SELECT c FROM Club c LEFT JOIN c.president p WHERE c.name LIKE %:keyword% OR c.slug LIKE %:keyword% OR p.name LIKE %:keyword%")
  List<Club> searchByKeyword(String keyword);
}
