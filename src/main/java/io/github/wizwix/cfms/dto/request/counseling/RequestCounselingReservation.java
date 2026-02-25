package io.github.wizwix.cfms.dto.request.counseling;

import java.time.LocalDate;
import java.time.LocalTime;

/// 상담 예약 신청 — POST /api/counseling/reservations 요청 DTO
public record RequestCounselingReservation(Long counselorId, LocalDate date, LocalTime startTime, LocalTime endTime,
                                          String topic, String memo) {}
