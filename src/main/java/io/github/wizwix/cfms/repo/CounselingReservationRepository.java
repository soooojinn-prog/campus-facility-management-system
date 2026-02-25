package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.Counselor;
import io.github.wizwix.cfms.model.CounselingReservation;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CounselingReservationRepository extends JpaRepository<CounselingReservation, Long> {
  List<CounselingReservation> findByUserAndStatusIn(User user, List<ReservationStatus> statuses);

  List<CounselingReservation> findByCounselorAndDate(Counselor counselor, LocalDate date);
}
