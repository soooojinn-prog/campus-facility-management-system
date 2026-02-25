package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.Counselor;
import io.github.wizwix.cfms.model.enums.CounselingDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CounselorRepository extends JpaRepository<Counselor, Long> {
  List<Counselor> findByDepartment(CounselingDepartment department);

  boolean existsByName(String name);
}
