package io.github.wizwix.cfms.repo.dorm;

import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.dorm.DormApplication;
import io.github.wizwix.cfms.model.dorm.DormRoom;
import io.github.wizwix.cfms.model.enums.DormApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DormApplicationRepository extends JpaRepository<DormApplication, Long> {
  boolean existsByApplicantAndSemesterAndStatusIn(User applicant, String semester, List<DormApplicationStatus> statuses);

  List<DormApplication> findByApplicantAndStatusIn(User applicant, List<DormApplicationStatus> statuses);

  List<DormApplication> findByRoomInAndSemesterAndStatusIn(List<DormRoom> rooms, String semester, List<DormApplicationStatus> statuses);
}
