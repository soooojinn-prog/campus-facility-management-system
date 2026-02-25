package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.CafeteriaMeal;
import io.github.wizwix.cfms.model.CafeteriaMealItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CafeteriaMealItemRepository extends JpaRepository<CafeteriaMealItem, Long> {
  List<CafeteriaMealItem> findByMealIn(List<CafeteriaMeal> meals);

  void deleteByMeal(CafeteriaMeal meal);
}
