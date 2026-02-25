package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.CafeteriaStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeteriaStoreRepository extends JpaRepository<CafeteriaStore, Long> {
  boolean existsByName(String name);

  Optional<CafeteriaStore> findByName(String name);
}
