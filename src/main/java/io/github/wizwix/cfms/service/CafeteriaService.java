package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.response.cafeteria.ResponseCafeteria;
import io.github.wizwix.cfms.dto.response.cafeteria.ResponseDiscount;
import io.github.wizwix.cfms.dto.response.cafeteria.ResponseFoodCourtStore;
import io.github.wizwix.cfms.dto.response.cafeteria.ResponseMeal;
import io.github.wizwix.cfms.dto.response.cafeteria.ResponseMealItem;
import io.github.wizwix.cfms.dto.response.cafeteria.ResponseStoreMenuItem;
import io.github.wizwix.cfms.model.cafeteria.CafeteriaMeal;
import io.github.wizwix.cfms.model.cafeteria.CafeteriaMealItem;
import io.github.wizwix.cfms.model.cafeteria.CafeteriaStore;
import io.github.wizwix.cfms.model.cafeteria.CafeteriaStoreMenu;
import io.github.wizwix.cfms.model.enums.CafeteriaMealType;
import io.github.wizwix.cfms.repo.cafeteria.CafeteriaMealItemRepository;
import io.github.wizwix.cfms.repo.cafeteria.CafeteriaMealRepository;
import io.github.wizwix.cfms.repo.cafeteria.CafeteriaStoreMenuRepository;
import io.github.wizwix.cfms.repo.cafeteria.CafeteriaStoreRepository;
import io.github.wizwix.cfms.service.iface.ICafeteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CafeteriaService implements ICafeteriaService {
  private final CafeteriaMealItemRepository mealItemRepository;
  private final CafeteriaMealRepository mealRepository;
  private final CafeteriaStoreMenuRepository storeMenuRepository;
  private final CafeteriaStoreRepository storeRepository;

  @Override
  public List<ResponseFoodCourtStore> getFoodCourtStores() {
    List<CafeteriaStore> stores = storeRepository.findAll();
    if (stores.isEmpty()) return List.of();

    List<CafeteriaStoreMenu> allMenus = storeMenuRepository.findByStoreIn(stores);
    Map<Long, List<CafeteriaStoreMenu>> menusByStoreId = allMenus.stream().collect(Collectors.groupingBy(menu -> menu.getStore().getId()));

    return stores.stream().map(store -> {
      List<ResponseStoreMenuItem> menuItems = menusByStoreId.getOrDefault(store.getId(), List.of()).stream().map(menu -> new ResponseStoreMenuItem(menu.getName(), menu.getPrice(), menu.getDiscountPrice() != null ? new ResponseDiscount(menu.getDiscountPrice(), menu.getDiscountLabel()) : null, Boolean.TRUE.equals(menu.getPopular()))).toList();
      return new ResponseFoodCourtStore(store.getId(), store.getName(), store.getDescription(), store.getCategory(), store.getRepresentative(), store.getHours(), menuItems);
    }).toList();
  }

  @Override
  public ResponseCafeteria getMeals(LocalDate date) {
    List<CafeteriaMeal> meals = mealRepository.findByDate(date);
    if (meals.isEmpty()) return new ResponseCafeteria(date, List.of());

    List<CafeteriaMealItem> allItems = mealItemRepository.findByMealIn(meals);
    Map<Long, List<CafeteriaMealItem>> itemsByMealId = allItems.stream().collect(Collectors.groupingBy(item -> item.getMeal().getId()));

    List<ResponseMeal> responseMeals = meals.stream().map(meal -> {
      List<ResponseMealItem> items = itemsByMealId.getOrDefault(meal.getId(), List.of()).stream().map(item -> new ResponseMealItem(item.getName(), item.getPrice(), item.getDiscountPrice() != null ? new ResponseDiscount(item.getDiscountPrice(), item.getDiscountLabel()) : null)).toList();
      return new ResponseMeal(toKorean(meal.getMealType()), meal.getTime(), meal.getIcon(), items);
    }).toList();

    return new ResponseCafeteria(date, responseMeals);
  }

  private String toKorean(CafeteriaMealType type) {
    return switch (type) {
      case BREAKFAST -> "조식";
      case LUNCH -> "중식";
      case DINNER -> "석식";
    };
  }
}
