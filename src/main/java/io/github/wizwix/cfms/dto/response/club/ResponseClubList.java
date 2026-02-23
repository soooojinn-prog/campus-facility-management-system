package io.github.wizwix.cfms.dto.response.club;

/// 동아리 정보
///
/// @param id            [Long] 동아리 ID (내부 인덱싱용)
/// @param name          [String] 동아리 이름
/// @param slug          [String] 동아리 URL 식별자
/// @param presidentName [String] 동아리 부장 이름
/// @param memberCount   [Integer] 동아리 부원 수
public record ResponseClubList(Long id, String name, String slug, String presidentName, Integer memberCount) {}
