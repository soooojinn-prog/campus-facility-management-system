package io.github.wizwix.cfms.dto.response.club;

import io.github.wizwix.cfms.model.enums.ClubRole;

import java.time.LocalDateTime;

/// 동아리 부원 정보
///
/// @param userId      [Long] 동아리 부원 ID (순수 인덱싱용)
/// @param name        [String] 부원 이름
/// @param userNumber [String] 부원 학번
/// @param role        [ClubRole] 부원의 역할
/// @param joinedAt    [LocalDateTime] 가입일 및 시각
public record ResponseClubMember(Long userId, String name, String userNumber, ClubRole role, LocalDateTime joinedAt) {}
