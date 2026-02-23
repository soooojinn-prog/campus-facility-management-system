package io.github.wizwix.cfms.dto.response.club;

import io.github.wizwix.cfms.model.enums.ClubStatus;

/// 동아리 상세 정보
///
/// @param id            [Long] 동아리 ID (내부 인덱싱용)
/// @param name          [String] 동아리 이름
/// @param slug          [String] 동아리 URL 식별자
/// @param description   [String] 동아리 소개문
/// @param presidentName [String] 동아리 부장
/// @param autoApprove   [Boolean] 동아리 가입 신청 시 자동 가입 여부
/// @param memberCount   [Integer] 동아리 부원 수
/// @param status        [ClubStatus] 동아리 상태
public record ResponseClubDetail(Long id, String name, String slug, String description, String presidentName,
                                 Boolean autoApprove, Integer memberCount, ClubStatus status) {}
