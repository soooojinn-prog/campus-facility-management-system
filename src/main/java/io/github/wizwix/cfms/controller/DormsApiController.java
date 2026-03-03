package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.dto.request.dorm.RequestDormApply;
import io.github.wizwix.cfms.dto.response.dorm.ResponseDormApplyResult;
import io.github.wizwix.cfms.dto.response.dorm.ResponseDormFloor;
import io.github.wizwix.cfms.dto.response.dorm.ResponseDormMyApplication;
import io.github.wizwix.cfms.model.enums.Gender;
import io.github.wizwix.cfms.service.iface.IDormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dorms")
@RequiredArgsConstructor
public class DormsApiController {
  private final IDormService dormService;

  /// 기숙사 입주 신청 (로그인 필요)
  @PostMapping("/applications")
  public ResponseEntity<ResponseDormApplyResult> application(Authentication auth, @Valid @RequestBody RequestDormApply request) {
    return ResponseEntity.ok(dormService.apply(auth.getName(), request));
  }

  /// 기숙사 신청 취소 — PENDING 상태인 신청만 취소 가능
  /// 취소 시 status를 CANCELLED로 변경 (soft delete)
  @DeleteMapping("/applications/{id}")
  public ResponseEntity<Void> cancel(Authentication auth, @PathVariable Long id) {
    dormService.cancelApplication(auth.getName(), id);
    return ResponseEntity.noContent().build();
  }

  /// 기숙사 호실 목록 (층별) — 성별 필터
  @GetMapping("/rooms")
  public ResponseEntity<List<ResponseDormFloor>> getDormRooms(@RequestParam Gender gender) {
    return ResponseEntity.ok(dormService.getDormRooms(gender));
  }

  /// 내 기숙사 신청 내역 조회 — 마이페이지 기숙사 탭에서 사용
  /// PENDING/APPROVED/REJECTED 상태만 반환 (CANCELLED는 제외)
  @GetMapping("/applications/me")
  public ResponseEntity<List<ResponseDormMyApplication>> myApplications(Authentication auth) {
    return ResponseEntity.ok(dormService.getMyApplications(auth.getName()));
  }
}
