package io.github.wizwix.cfms.dto.response.club;

import io.github.wizwix.cfms.model.enums.ClubStatus;

import java.time.LocalDateTime;

/// 동아리 상세 정보 (동아리 상세 페이지, 관리자 동아리 처리 목록)
///
/// 단순 목록 표시용으로 적합하지 않음! 관리자 동아리 처리 목록의 경우, 관리자가 모든 정보를
/// 알아야 하기 때문에 모든 정보를 보냄
///
/// @param id            [Long] 동아리 ID (내부 인덱싱용)
/// @param name          [String] 동아리 이름
/// @param slug          [String] 동아리 URL 식별자
/// @param description   [String] 동아리 소개문
/// @param presidentName [String] 동아리 부장
/// @param autoApprove   [Boolean] 동아리 가입 신청 시 자동 가입 여부
/// @param memberCount   [Integer] 동아리 부원 수
/// @param createdAt     [LocalDateTime] 동아리 생성일
/// @param status        [ClubStatus] 동아리 상태
public record ResponseClubDetail(Long id, String name, String slug, String description, String presidentName,
                                 Boolean autoApprove, Integer memberCount, LocalDateTime createdAt,
                                 ClubStatus status) {}
