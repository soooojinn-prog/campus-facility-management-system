package io.github.wizwix.cfms.repo.library;

import io.github.wizwix.cfms.model.library.LibraryStudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibraryStudyRoomRepository extends JpaRepository<LibraryStudyRoom, Long> {
  boolean existsByName(String name);

  Optional<LibraryStudyRoom> findByName(String name);
}
