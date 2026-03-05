package io.github.wizwix.cfms.dto.response.counseling;

import io.github.wizwix.cfms.model.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/// 상담 예약 정보 — GET /api/counseling/reservations/me, GET /api/counseling/slots 응답 DTO
public record ResponseCounselingReservation(Long id, String counselorName, String department, LocalDate date,
                                            LocalTime startTime, LocalTime endTime, ReservationStatus status,
                                            String topic, String memo, String rejectReason,
                                            LocalDateTime createdAt) {}
