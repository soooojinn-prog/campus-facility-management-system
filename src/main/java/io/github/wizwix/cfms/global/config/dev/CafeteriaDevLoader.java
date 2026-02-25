package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.wizwix.cfms.global.config.dev.base.DevDataLoader;
import io.github.wizwix.cfms.model.CafeteriaMeal;
import io.github.wizwix.cfms.model.CafeteriaMealItem;
import io.github.wizwix.cfms.model.CafeteriaStore;
import io.github.wizwix.cfms.model.CafeteriaStoreMenu;
import io.github.wizwix.cfms.model.enums.MealType;
import io.github.wizwix.cfms.repo.CafeteriaMealItemRepository;
import io.github.wizwix.cfms.repo.CafeteriaMealRepository;
import io.github.wizwix.cfms.repo.CafeteriaStoreMenuRepository;
import io.github.wizwix.cfms.repo.CafeteriaStoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDate;

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

  public CafeteriaDevLoader(ResourceLoader resourceLoader,
                            CafeteriaStoreRepository storeRepo, CafeteriaStoreMenuRepository menuRepo,
                            CafeteriaMealRepository mealRepo, CafeteriaMealItemRepository mealItemRepo) {
    this.resourceLoader = resourceLoader;
    this.mapper = new ObjectMapper();
    this.storeRepo = storeRepo;
    this.menuRepo = menuRepo;
    this.mealRepo = mealRepo;
    this.mealItemRepo = mealItemRepo;
  }

  @Override
  public void load() {
    if (storeRepo.count() > 0) return;
    loadStores();
    loadMeals();
    log.info("Loaded dev cafeteria data: {} stores, {} meals", storeRepo.count(), mealRepo.count());
  }

  @Override
  public void unload() {
    mealItemRepo.deleteAll();
    mealRepo.deleteAll();
    menuRepo.deleteAll();
    storeRepo.deleteAll();
    log.info("Unloaded dev cafeteria data");
  }

  private void loadStores() {
    JsonNode root = readJson("data/dev/cafeteria-stores.jsonc");
    if (root == null) return;

    for (JsonNode node : root) {
      CafeteriaStore store = new CafeteriaStore();
      store.setName(node.get("name").asText());
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

  private void loadMeals() {
    JsonNode root = readJson("data/dev/cafeteria-meals.jsonc");
    if (root == null) return;

    LocalDate today = LocalDate.now();

    for (JsonNode node : root) {
      CafeteriaMeal meal = new CafeteriaMeal();
      meal.setDate(today);
      meal.setMealType(MealType.valueOf(node.get("mealType").asText()));
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
    }
  }
}
