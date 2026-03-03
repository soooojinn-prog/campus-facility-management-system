package io.github.wizwix.cfms.repo.library;

import io.github.wizwix.cfms.model.library.LibraryReadingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibraryReadingRoomRepository extends JpaRepository<LibraryReadingRoom, Long> {
  boolean existsByName(String name);

  Optional<LibraryReadingRoom> findByName(String name);
}
