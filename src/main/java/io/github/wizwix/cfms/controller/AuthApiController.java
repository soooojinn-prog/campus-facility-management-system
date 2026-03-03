package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.dto.request.auth.RequestLogin;
import io.github.wizwix.cfms.dto.request.auth.RequestPasswordResetConfirm;
import io.github.wizwix.cfms.dto.request.auth.RequestPasswordResetRequest;
import io.github.wizwix.cfms.dto.request.auth.RequestPasswordResetVerify;
import io.github.wizwix.cfms.dto.request.auth.RequestRegister;
import io.github.wizwix.cfms.dto.response.auth.ResponseLogin;
import io.github.wizwix.cfms.global.security.JwtUtils;
import io.github.wizwix.cfms.service.iface.IUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {
  private final JwtUtils jwtUtils;
  private final IUserService userService;

  /// 로그인
  @PostMapping("/login")
  public ResponseEntity<ResponseLogin> login(@Valid @RequestBody RequestLogin req, HttpServletResponse response) {
    ResponseLogin result = userService.login(req);

    Cookie cookie = new Cookie("jwt_token", result.accessToken());
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge((int) jwtUtils.getExpiration() / 1000);

    response.addCookie(cookie);

    return ResponseEntity.ok(result);
  }

  /// 로그아웃
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse resp) {
    Cookie cookie = new Cookie("jwt_token", null);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);

    resp.addCookie(cookie);

    return ResponseEntity.noContent().build();
    // `/`로 리다이렉션하는 것은 프론트엔드의 몫
  }

  /// 비밀번호 재설정 (3단계)
  @PatchMapping("/password-reset/confirm")
  public ResponseEntity<Void> passwordResetConfirm(@Valid @RequestBody RequestPasswordResetConfirm request) {
    userService.confirmPasswordReset(request);
    return ResponseEntity.ok().build();
  }

  /// 비밀번호 재설정 (1단계)
  @PostMapping("/password-reset")
  public ResponseEntity<Void> passwordResetRequest(@Valid @RequestBody RequestPasswordResetRequest request) {
    userService.requestPasswordReset(request);
    return ResponseEntity.ok().build();
  }

  /// 비밀번호 재설정 (2단계)
  @PostMapping("/password-reset/verify")
  public ResponseEntity<Void> passwordResetVerify(@Valid @RequestBody RequestPasswordResetVerify request) {
    userService.verifyPasswordReset(request);
    return ResponseEntity.ok().build();
  }

  /// 회원가입
  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody RequestRegister req) {
    userService.register(req);
    return ResponseEntity.status(HttpStatus.CREATED).header("Location", "/login").build();
  }
}
