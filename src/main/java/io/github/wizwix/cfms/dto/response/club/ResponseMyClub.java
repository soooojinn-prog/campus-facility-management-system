package io.github.wizwix.cfms.dto.response.club;

import io.github.wizwix.cfms.model.enums.ClubMemberStatus;
import io.github.wizwix.cfms.model.enums.ClubRole;
import io.github.wizwix.cfms.model.enums.ClubStatus;

import java.time.LocalDateTime;

/// 마이페이지 "내 동아리" 탭용 DTO
public record ResponseMyClub(
    Long clubId, String clubName, String slug, String presidentName,
    ClubStatus clubStatus, ClubRole myRole, ClubMemberStatus memberStatus,
    LocalDateTime joinedAt
) {}
