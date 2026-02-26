package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Room r WHERE CONCAT(r.building.name, ' ', r.name) = :fullName")
  boolean existsByFullName(String fullName);

  @Query("SELECT r FROM Room r WHERE CONCAT(r.building.name, ' ', r.name) = :fullName")
  Optional<Room> findByFullName(String fullName);

  List<Room> getRoomsByBuildingSlug(String slug);
}
