package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.CafeteriaMeal;
import io.github.wizwix.cfms.model.enums.MealType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CafeteriaMealRepository extends JpaRepository<CafeteriaMeal, Long> {
  boolean existsByDateAndMealType(LocalDate date, MealType mealType);

  List<CafeteriaMeal> findByDate(LocalDate date);

  Optional<CafeteriaMeal> findByDateAndMealType(LocalDate date, MealType mealType);

  /// unload 시 날짜 무관하게 mealType으로 전체 조회 (이전 날짜 잔존 데이터 정리용)
  List<CafeteriaMeal> findByMealType(MealType mealType);
}
