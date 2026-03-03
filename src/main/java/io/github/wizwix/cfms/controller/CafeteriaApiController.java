package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.dto.response.cafeteria.ResponseCafeteria;
import io.github.wizwix.cfms.dto.response.cafeteria.ResponseFoodCourtStore;
import io.github.wizwix.cfms.service.iface.ICafeteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cafeterias")
@RequiredArgsConstructor
public class CafeteriaApiController {
  private final ICafeteriaService cafeteriaService;

  /// 푸드코트 가게 전체
  @GetMapping("/foodcourt")
  public ResponseEntity<List<ResponseFoodCourtStore>> getFoodCourtStores() {
    return ResponseEntity.ok(cafeteriaService.getFoodCourtStores());
  }

  /// 오늘의 학식 (date 파라미터 없으면 오늘 날짜)
  @GetMapping("/meals")
  public ResponseEntity<ResponseCafeteria> getMeals(@RequestParam(required = false) LocalDate date) {
    if (date == null) date = LocalDate.now();
    return ResponseEntity.ok(cafeteriaService.getMeals(date));
  }
}
