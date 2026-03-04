package io.github.wizwix.cfms.dto.response.dorm;

import io.github.wizwix.cfms.model.enums.DormApplicationStatus;
import io.github.wizwix.cfms.model.enums.DormPeriod;

import java.time.LocalDateTime;

/// 내 기숙사 신청 내역 — GET /api/dorms/my 응답 DTO
/// 마이페이지 기숙사 탭에서 테이블 한 행에 대응
///
/// @param id           [Long] 신청 ID (취소 시 DELETE /api/dorms/{id}에 사용)
/// @param roomNumber   [String] 호실 번호 (e.g. "M103")
/// @param semester     [String] 학기 (e.g. "2026-1")
/// @param period       [DormPeriod] 입주 기간 (SEMESTER / YEAR)
/// @param status       [DormApplicationStatus] 신청 상태 (PENDING / APPROVED / REJECTED)
/// @param studentsName [String] 룸메이트 이름
/// @param createdAt    [LocalDateTime] 신청 일시
public record ResponseDormMyApplication(Long id, String roomNumber, String semester, DormPeriod period,
                                        DormApplicationStatus status, String studentsName, LocalDateTime createdAt) {}
