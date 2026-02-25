package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.DormRoom;
import io.github.wizwix.cfms.model.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DormRoomRepository extends JpaRepository<DormRoom, Long> {
  boolean existsByRoomNumber(String roomNumber);

  List<DormRoom> findByGender(Gender gender);

  List<DormRoom> findByGenderAndFloor(Gender gender, int floor);

  Optional<DormRoom> findByRoomNumber(String roomNumber);
}
