package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.dto.response.building.ResponseBuilding;
import io.github.wizwix.cfms.dto.response.room.ResponseRoom;
import io.github.wizwix.cfms.service.iface.IBuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingApiController {
  private final IBuildingService buildingService;

  /// 전체 건물 목록 조회
  @GetMapping
  public ResponseEntity<List<ResponseBuilding>> listAll() {
    return ResponseEntity.ok(buildingService.listAll());
  }

  /// 건물의 방 목록
  @GetMapping("/{slug}/rooms")
  public ResponseEntity<List<ResponseRoom>> listRooms(@PathVariable String slug) {
    return ResponseEntity.ok(buildingService.getRoomsBySlug(slug));
  }
}
