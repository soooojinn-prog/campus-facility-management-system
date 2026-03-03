package io.github.wizwix.cfms.repo.counceling;

import io.github.wizwix.cfms.model.counseling.Counselor;
import io.github.wizwix.cfms.model.enums.CounselingDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CounselorRepository extends JpaRepository<Counselor, Long> {
  boolean existsByName(String name);

  List<Counselor> findByDepartment(CounselingDepartment department);

  Optional<Counselor> findByName(String name);
}
