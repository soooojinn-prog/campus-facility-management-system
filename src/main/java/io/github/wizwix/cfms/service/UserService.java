package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.request.auth.RequestLogin;
import io.github.wizwix.cfms.dto.request.auth.RequestPasswordResetConfirm;
import io.github.wizwix.cfms.dto.request.auth.RequestPasswordResetRequest;
import io.github.wizwix.cfms.dto.request.auth.RequestPasswordResetVerify;
import io.github.wizwix.cfms.dto.request.auth.RequestRegister;
import io.github.wizwix.cfms.dto.request.auth.RequestUserUpdate;
import io.github.wizwix.cfms.dto.response.auth.ResponseLogin;
import io.github.wizwix.cfms.dto.response.auth.ResponseUserProfile;
import io.github.wizwix.cfms.dto.response.auth.ResponseUserSimpleInfo;
import io.github.wizwix.cfms.global.security.JwtUtils;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.enums.UserRole;
import io.github.wizwix.cfms.repo.UserRepository;
import io.github.wizwix.cfms.service.iface.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService implements IUserService {
  private final JwtUtils jwtUtils;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  @Override
  public void confirmPasswordReset(RequestPasswordResetConfirm request) {
    Optional<User> optUser = userRepository.findByNumberAndEnabledTrue(request.userNumber());
    if (optUser.isEmpty()) return; // Return HTTP 200 when user was not found
    User user = optUser.get();

    if (user.getPasswordResetToken() == null || !passwordEncoder.matches(request.token(), user.getPasswordResetToken())) {
      throw new IllegalArgumentException("Invalid reset token");
    }
    if (user.getPasswordResetTokenExpiry() == null || user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("Reset token expired");
    }

    user.setPassword(passwordEncoder.encode(request.newPassword()));

    user.setPasswordResetToken(null);
    user.setPasswordResetTokenExpiry(null);

    // `.save` is redundant but I'll make sure the line exists
    userRepository.save(user);
  }

  /// 마이페이지 프로필 조회 — GET /api/users/me 에서 호출
  /// JWT의 subject(학번)로 사용자를 찾아 ResponseUserProfile(gender 포함)로 반환
  @Override
  @Transactional(readOnly = true)
  public ResponseUserProfile getProfile(String userNumber) {
    User user = userRepository.findByNumberAndEnabledTrue(userNumber).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    return new ResponseUserProfile(user.getId(), user.getName(), user.getNumber(), user.getEmail(), user.getRole(), user.getGender(), user.getCreatedAt());
  }

  @Override
  public ResponseLogin login(RequestLogin login) {
    User user = userRepository.findByNumberAndEnabledTrue(login.userNumber()).orElseThrow(() -> new IllegalArgumentException("User not found"));
    if (!passwordEncoder.matches(login.password(), user.getPassword())) {
      throw new IllegalArgumentException("Invalid password");
    }
    String token = jwtUtils.generateToken(user.getNumber(), List.of(user.getRole().name()));

    ResponseUserSimpleInfo userInfo = new ResponseUserSimpleInfo(user.getId(), user.getName(), user.getNumber(), user.getRole(), user.getGender());

    return new ResponseLogin(token, userInfo);
  }

  @Override
  public void register(RequestRegister request) {
    if (userRepository.findByNumber(request.userNumber()).isPresent()) {
      throw new IllegalArgumentException("이미 존재하는 학번/교번입니다.");
    }

    User user = new User();
    user.setNumber(request.userNumber());
    user.setName(request.name());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setEmail(request.email());
    user.setCreatedAt(LocalDateTime.now());
    if (request.role() != null && request.role() == UserRole.ROLE_ADMIN) {
      throw new IllegalArgumentException("관리자 계정은 직접 가입할 수 없습니다.");
    }
    user.setRole(request.role() != null ? request.role() : UserRole.ROLE_STUDENT);
    if (request.gender() != null) {
      user.setGender(request.gender());
    }

    // `.save` is redundant but I'll make sure the line exists
    userRepository.save(user);
  }

  @Override
  public void requestPasswordReset(RequestPasswordResetRequest request) {
    User user = userRepository.findByNumberAndEmailAndEnabledTrue(request.userNumber(), request.email()).orElseThrow(() -> new IllegalArgumentException("User not found"));
    SecureRandom random = new SecureRandom();
    String token = String.format("%06d", random.nextInt(1_000_000));
    user.setPasswordResetToken(passwordEncoder.encode(token));
    user.setPasswordResetTokenExpiry(LocalDateTime.now().plusMinutes(10)); // 인증 토큰은 10분동안 유효함
    // `.save` is redundant but I'll make sure the line exists
    userRepository.save(user);

    log.info("======== PASSWORD RESET TOKEN ========");
    log.info("User: {}", user.getNumber());
    log.info("Token: {}", token);
    log.info("======================================");
  }

  /// 마이페이지 정보 수정 — PATCH /api/users/me 에서 호출
  /// 기존 비밀번호(oldPassword) 검증을 통과해야 변경 가능
  /// newPassword, email, gender 중 값이 있는 필드만 선택적으로 업데이트
  @Override
  public void updateProfile(String userNumber, RequestUserUpdate request) {
    User user = userRepository.findByNumberAndEnabledTrue(userNumber).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    // 기존 비밀번호 필수 검증 — 본인 확인 용도
    if (request.oldPassword() == null || !passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
      throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
    }
    if (request.newPassword() != null && !request.newPassword().isBlank()) {
      user.setPassword(passwordEncoder.encode(request.newPassword()));
    }
    if (request.email() != null && !request.email().isBlank()) {
      user.setEmail(request.email());
    }
    // 기존 가입자 중 성별이 NULL인 경우가 있어서 마이페이지에서 수정 가능하도록 추가
    if (request.gender() != null) {
      user.setGender(request.gender());
    }
    userRepository.save(user);
  }

  @Override
  public void verifyPasswordReset(RequestPasswordResetVerify request) {
    User user = userRepository.findByNumberAndEnabledTrue(request.userNumber()).orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (user.getPasswordResetToken() == null) {throw new IllegalArgumentException("No reset token found");}
    if (!passwordEncoder.matches(request.token(), user.getPasswordResetToken())) {
      throw new IllegalArgumentException("Invalid reset token");
    }
    if (user.getPasswordResetTokenExpiry() == null || user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
      throw new IllegalArgumentException("Reset token expired");
    }
  }
}
