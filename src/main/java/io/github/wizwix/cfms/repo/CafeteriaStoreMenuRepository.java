package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.CafeteriaStore;
import io.github.wizwix.cfms.model.CafeteriaStoreMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CafeteriaStoreMenuRepository extends JpaRepository<CafeteriaStoreMenu, Long> {
  /// Spring Data JPA의 커스텀 derived delete 메서드는
  /// 기본 제공 delete()와 달리 @Transactional이 자동 적용되지 않음
  @Transactional
  void deleteByStore(CafeteriaStore store);

  List<CafeteriaStoreMenu> findByStoreIn(List<CafeteriaStore> stores);
}
