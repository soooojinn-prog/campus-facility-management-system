package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.dto.request.auth.RequestUserUpdate;
import io.github.wizwix.cfms.dto.response.auth.ResponseUserProfile;
import io.github.wizwix.cfms.service.iface.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {
  private final IUserService userService;

  /// 내 프로필 조회 — 마이페이지 개인정보 탭에서 사용
  /// Authentication에서 JWT subject(학번)를 꺼내서 UserService에 전달
  @GetMapping("/me")
  public ResponseEntity<ResponseUserProfile> me(Authentication auth) {
    return ResponseEntity.ok(userService.getProfile(auth.getName()));
  }

  /// 사용자 프로필 조회 (미구현 스텁)
  @GetMapping("/{id}")
  public ResponseEntity<ResponseUserProfile> profile(@PathVariable String id) {
    return ResponseEntity.ok(userService.getProfile(id));
  }

  /// 내 정보 수정 (비밀번호, 이메일, 성별) — 마이페이지 정보 수정 폼에서 사용
  /// RequestUserUpdate: { oldPassword(필수), newPassword, email, gender }
  @PatchMapping("/me")
  public ResponseEntity<Void> update(Authentication auth, @RequestBody RequestUserUpdate request) {
    userService.updateProfile(auth.getName(), request);
    return ResponseEntity.ok().build();
  }
}
