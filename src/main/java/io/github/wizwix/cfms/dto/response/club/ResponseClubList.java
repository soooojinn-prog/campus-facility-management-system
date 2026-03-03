package io.github.wizwix.cfms.dto.response.club;

import io.github.wizwix.cfms.model.enums.ClubStatus;

import java.time.LocalDateTime;

/// 동아리 정보 (동아리 목록 표시용)
///
/// 동아리 목록 표시용으로 경량화된 정보. 목록에서 '동아리 소개' 등을 표시할 이유가 없으므로,
/// 해당 필드들을 제거함. 생략된 필드를 확인하기 위해서는 동아리 상세 페이지 등으로 직접 접근해야 함
///
/// @param id            [Long] 동아리 ID (내부 인덱싱용)
/// @param name          [String] 동아리 이름
/// @param slug          [String] 동아리 URL 식별자
/// @param presidentName [String] 동아리 부장 이름
/// @param status        [ClubStatus] 동아리 상태
/// @param memberCount   [Integer] 동아리 부원 수
/// @param createdAt     [LocalDateTime] 동아리 창설 일시
public record ResponseClubList(Long id, String name, String slug, String presidentName,
                               ClubStatus status, Integer memberCount, LocalDateTime createdAt) {}
