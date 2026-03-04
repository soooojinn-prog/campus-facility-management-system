package io.github.wizwix.cfms.dto.request.auth;

import io.github.wizwix.cfms.model.enums.Gender;
import io.github.wizwix.cfms.model.enums.UserRole;
import jakarta.validation.constraints.NotNull;

/// 회원 가입 요청
///
/// 이 프로젝트는 이미 학생과 교사 정보를 갖고 있는 대학이 서비스하는 것을 전제로 삼았기에, 회원 가입은 본래 불가능해야 함.
/// 하지만 프로젝트 시연을 위해 임의로 회원 가입 요청을 도입함
///
/// @param userNumber [String] 교번 or 학번
/// @param name       [String] 이름
/// @param password   [String] 비밀번호
/// @param email      [String] 이메일 주소
/// @param gender     [Gender] 성별
public record RequestRegister(String userNumber, String name, String password, String email, @NotNull Gender gender, UserRole role) {}
