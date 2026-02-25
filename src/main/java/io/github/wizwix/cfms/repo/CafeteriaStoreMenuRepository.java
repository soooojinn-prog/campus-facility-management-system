package io.github.wizwix.cfms.repo;

import io.github.wizwix.cfms.model.CafeteriaStore;
import io.github.wizwix.cfms.model.CafeteriaStoreMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CafeteriaStoreMenuRepository extends JpaRepository<CafeteriaStoreMenu, Long> {
  List<CafeteriaStoreMenu> findByStoreIn(List<CafeteriaStore> stores);

  void deleteByStore(CafeteriaStore store);
}
