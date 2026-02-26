package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.CafeteriaMeal;
import io.github.wizwix.cfms.model.CafeteriaMealItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CafeteriaMealItemRepository extends JpaRepository<CafeteriaMealItem, Long> {
  /// Spring Data JPA의 커스텀 derived delete 메서드는
  /// 기본 제공 delete()와 달리 @Transactional이 자동 적용되지 않음
  @Transactional
  void deleteByMeal(CafeteriaMeal meal);

  List<CafeteriaMealItem> findByMealIn(List<CafeteriaMeal> meals);
}
