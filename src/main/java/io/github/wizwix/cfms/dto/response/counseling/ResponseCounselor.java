package io.github.wizwix.cfms.dto.response.counseling;

import io.github.wizwix.cfms.model.enums.CounselingDepartment;

/// 상담사 정보 — GET /api/counseling/counselors 응답 DTO
public record ResponseCounselor(Long id, String name, CounselingDepartment department, String position,
                                String specialization) {}
