package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.CafeteriaMeal;
import io.github.wizwix.cfms.model.enums.MealType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CafeteriaMealRepository extends JpaRepository<CafeteriaMeal, Long> {
  List<CafeteriaMeal> findByDate(LocalDate date);

  boolean existsByDateAndMealType(LocalDate date, MealType mealType);

  Optional<CafeteriaMeal> findByDateAndMealType(LocalDate date, MealType mealType);
}
