package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.Building;
import io.github.wizwix.cfms.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
  boolean existsByBuildingAndName(Building building, String name);

  List<Room> getRoomsByBuildingSlug(String slug);
}
