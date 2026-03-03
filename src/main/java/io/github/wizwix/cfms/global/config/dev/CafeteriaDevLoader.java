package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.DevDataLoader;
import io.github.wizwix.cfms.model.cafeteria.CafeteriaMeal;
import io.github.wizwix.cfms.model.cafeteria.CafeteriaMealItem;
import io.github.wizwix.cfms.model.cafeteria.CafeteriaStore;
import io.github.wizwix.cfms.model.cafeteria.CafeteriaStoreMenu;
import io.github.wizwix.cfms.model.enums.CafeteriaMealType;
import io.github.wizwix.cfms.repo.cafeteria.CafeteriaMealItemRepository;
import io.github.wizwix.cfms.repo.cafeteria.CafeteriaMealRepository;
import io.github.wizwix.cfms.repo.cafeteria.CafeteriaStoreMenuRepository;
import io.github.wizwix.cfms.repo.cafeteria.CafeteriaStoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

@Component
@Profile("dev")
@Slf4j
public class CafeteriaDevLoader implements DevDataLoader {
  private final ObjectMapper mapper;
  private final CafeteriaMealItemRepository mealItemRepo;
  private final CafeteriaMealRepository mealRepo;
  private final CafeteriaStoreMenuRepository menuRepo;
  private final ResourceLoader resourceLoader;
  private final CafeteriaStoreRepository storeRepo;

  public CafeteriaDevLoader(ResourceLoader resourceLoader, ObjectMapper mapper, CafeteriaStoreRepository storeRepo, CafeteriaStoreMenuRepository menuRepo, CafeteriaMealRepository mealRepo, CafeteriaMealItemRepository mealItemRepo) {
    this.resourceLoader = resourceLoader;
    this.mapper = mapper;
    this.storeRepo = storeRepo;
    this.menuRepo = menuRepo;
    this.mealRepo = mealRepo;
    this.mealItemRepo = mealItemRepo;
  }

  @Override
  public void load() {
    if (storeRepo.count() > 0) return;

    // loadStores();
    // loadMeals();
    // log.info("Loaded dev cafeteria data: {} stores, {} meals", storeRepo.count(), mealRepo.count());

    LongAdder store = new LongAdder();
    LongAdder meal = new LongAdder();

    // Store, Menu 로드
    List<CafeteriaStore> stores = readJson("data/dev/cafeteria-stores.jsonc", new TypeReference<>() {});
    if (stores != null) {
      stores.forEach(s -> {
        if (s.getMenus() != null) s.getMenus().forEach(m -> m.setStore(s));
        store.increment();
      });
      storeRepo.saveAll(stores);
      log.info("Dev Profile: Loaded {} cafeteria stores", store.sum());
    }

    //
    List<CafeteriaMeal> meals = readJson("data/dev/cafeteria-meals.jsonc", new TypeReference<>() {});
    if (meals != null) {
      LocalDate today = LocalDate.now();
      meals.forEach(m -> {
        m.setDate(today);
        if (m.getItems() != null) m.getItems().forEach(i -> i.setMeal(m));
        meal.increment();
      });
      mealRepo.saveAll(meals);
      log.info("Dev Profile: Loaded {} cafeteria meals", meal.sum());
    }
  }

  @Override
  public void unload() {
    // unloadMeals();
    // unloadStores();

    LongAdder store = new LongAdder();
    LongAdder meal = new LongAdder();

    // Store, Menu 로드
    List<CafeteriaStore> stores = readJson("data/dev/cafeteria-stores.jsonc", new TypeReference<>() {});
    if (stores != null) {
      stores.forEach(s -> {
        storeRepo.findByName(s.getName()).ifPresent(existing -> {
          storeRepo.delete(existing);
          store.increment();
        });
      });
      log.info("Dev Profile: Unloaded {} cafeteria stores", store.sum());
    }

    //
    List<CafeteriaMeal> meals = readJson("data/dev/cafeteria-meals.jsonc", new TypeReference<>() {});
    if (meals != null) {
      meals.forEach(t -> {
        List<CafeteriaMeal> existings = mealRepo.findByMealType(t.getMealType());
        if (!existings.isEmpty()) {
          mealRepo.deleteAll(existings);
          meal.increment();
        }
      });
      log.info("Dev Profile: Unloaded {} cafeteria meals", meal.sum());
    }
  }

  private <T> T readJson(String path, TypeReference<T> type) {
    try (InputStream is = resourceLoader.getResource("classpath:" + path).getInputStream()) {
      return mapper.readValue(is, type);
    } catch (Exception e) {
      log.error("Failed to load {}", path, e);
      return null;
    }
  }

  private void loadMeals() {
    JsonNode root = readJson("data/dev/cafeteria-meals.jsonc");
    if (root == null) return;

    LocalDate today = LocalDate.now();

    for (JsonNode node : root) {
      CafeteriaMealType mealType = CafeteriaMealType.valueOf(node.get("mealType").asText());
      if (mealRepo.existsByDateAndMealType(today, mealType)) continue;

      CafeteriaMeal meal = new CafeteriaMeal();
      meal.setDate(today);
      meal.setMealType(mealType);
      meal.setTime(node.get("time").asText());
      meal.setIcon(node.get("icon").asText());
      mealRepo.save(meal);

      for (JsonNode itemNode : node.get("items")) {
        CafeteriaMealItem item = new CafeteriaMealItem();
        item.setMeal(meal);
        item.setName(itemNode.get("name").asText());
        item.setPrice(itemNode.get("price").asInt());
        item.setDiscountPrice(itemNode.get("discountPrice").isNull() ? null : itemNode.get("discountPrice").asInt());
        item.setDiscountLabel(itemNode.get("discountLabel").isNull() ? null : itemNode.get("discountLabel").asText());
        mealItemRepo.save(item);
      }
      log.info("Loaded dev cafeteria meal: ({} / {} / {})", mealType, meal.getTime(), meal.getIcon());
    }
  }

  private JsonNode readJson(String path) {
    try {
      Resource resource = resourceLoader.getResource("classpath:" + path);
      try (InputStream is = resource.getInputStream()) {
        return mapper.readTree(is);
      }
    } catch (Exception e) {
      log.error("Error reading JSON from {}", path, e);
      return null;
    }
  }

  private void loadStores() {
    JsonNode root = readJson("data/dev/cafeteria-stores.jsonc");
    if (root == null) return;

    for (JsonNode node : root) {
      String name = node.get("name").asText();
      if (storeRepo.existsByName(name)) continue;

      CafeteriaStore store = new CafeteriaStore();
      store.setName(name);
      store.setDescription(node.get("description").asText());
      store.setCategory(node.get("category").asText());
      store.setRepresentative(node.get("representative").asText());
      store.setHours(node.get("hours").asText());
      storeRepo.save(store);

      for (JsonNode menuNode : node.get("menus")) {
        CafeteriaStoreMenu menu = new CafeteriaStoreMenu();
        menu.setStore(store);
        menu.setName(menuNode.get("name").asText());
        menu.setPrice(menuNode.get("price").asInt());
        menu.setDiscountPrice(menuNode.get("discountPrice").isNull() ? null : menuNode.get("discountPrice").asInt());
        menu.setDiscountLabel(menuNode.get("discountLabel").isNull() ? null : menuNode.get("discountLabel").asText());
        menu.setPopular(menuNode.get("popular").asBoolean());
        menuRepo.save(menu);
      }
      log.info("Loaded dev cafeteria store: ({} / {} / {})", store.getName(), store.getCategory(), store.getRepresentative());
    }
  }

  /// meal 데이터 정리.
  /// 기존에는 LocalDate.now()로 '오늘' 데이터만 삭제했으나,
  /// 앱을 다른 날짜에 실행했던 데이터가 남는 문제가 있어서
  /// mealType 기준으로 모든 날짜의 데이터를 삭제하도록 변경.
  private void unloadMeals() {
    JsonNode root = readJson("data/dev/cafeteria-meals.jsonc");
    if (root == null) return;

    for (JsonNode node : root) {
      CafeteriaMealType mealType = CafeteriaMealType.valueOf(node.get("mealType").asText());
      List<CafeteriaMeal> meals = mealRepo.findByMealType(mealType);
      for (CafeteriaMeal existing : meals) {
        mealItemRepo.deleteByMeal(existing);
        mealRepo.delete(existing);
        log.info("Unloaded dev cafeteria meal: ({} / {})", mealType, existing.getDate());
      }
    }
  }

  private void unloadStores() {
    JsonNode root = readJson("data/dev/cafeteria-stores.jsonc");
    if (root == null) return;

    for (JsonNode node : root) {
      String name = node.get("name").asText();
      storeRepo.findByName(name).ifPresent(existing -> {
        menuRepo.deleteByStore(existing);
        storeRepo.delete(existing);
        log.info("Unloaded dev cafeteria store: ({} / {})", existing.getName(), existing.getCategory());
      });
    }
  }
}
