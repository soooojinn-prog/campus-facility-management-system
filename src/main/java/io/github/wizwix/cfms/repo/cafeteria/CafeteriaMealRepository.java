package io.github.wizwix.cfms.repo.cafeteria;

import io.github.wizwix.cfms.model.cafeteria.CafeteriaMeal;
import io.github.wizwix.cfms.model.enums.CafeteriaMealType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CafeteriaMealRepository extends JpaRepository<CafeteriaMeal, Long> {
  boolean existsByDateAndMealType(LocalDate date, CafeteriaMealType mealType);

  List<CafeteriaMeal> findByDate(LocalDate date);

  Optional<CafeteriaMeal> findByDateAndMealType(LocalDate date, CafeteriaMealType mealType);

  /// unload 시 날짜 무관하게 mealType으로 전체 조회 (이전 날짜 잔존 데이터 정리용)
  List<CafeteriaMeal> findByMealType(CafeteriaMealType mealType);
}
