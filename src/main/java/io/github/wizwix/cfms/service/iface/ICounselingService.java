package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.request.counseling.RequestCounselingReservation;
import io.github.wizwix.cfms.dto.response.counseling.ResponseCounselingReservation;
import io.github.wizwix.cfms.dto.response.counseling.ResponseCounselor;
import io.github.wizwix.cfms.model.enums.CounselingDepartment;

import java.time.LocalDate;
import java.util.List;

public interface ICounselingService {
  void cancelReservation(String userNumber, Long reservationId);

  ResponseCounselingReservation createReservation(String userNumber, RequestCounselingReservation request);

  List<ResponseCounselor> getCounselors(CounselingDepartment dept);

  List<ResponseCounselingReservation> getMyReservations(String userNumber);

  List<ResponseCounselingReservation> getSlots(Long counselorId, LocalDate date);
}
