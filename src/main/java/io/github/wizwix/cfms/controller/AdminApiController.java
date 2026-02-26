package io.github.wizwix.cfms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController {
  @GetMapping("/clubs")
  public ResponseEntity<?> getClubs(@RequestParam String status) {
//    return ResponseEntity.ok(List.of());
    return ResponseEntity.ok("""
        [
          {
            "id": 1,
            "name": "동아리_이름",
            "description": "엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf엄청 긴 소개문asdf",
            "slug": "test",
            "president": "회장님",
            "status": "PENDING",
            "createdAt": "2026-02-01T00:00:00"
          }
        ]
        """);
  }
}
