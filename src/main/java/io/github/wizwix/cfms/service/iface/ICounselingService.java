package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.request.counseling.RequestCounselingReservation;
import io.github.wizwix.cfms.dto.response.counseling.ResponseCounselor;
import io.github.wizwix.cfms.dto.response.counseling.ResponseCounselingReservation;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.enums.CounselingDepartment;

import java.time.LocalDate;
import java.util.List;

public interface ICounselingService {
  List<ResponseCounselor> getCounselors(CounselingDepartment dept);

  List<ResponseCounselingReservation> getSlots(Long counselorId, LocalDate date);

  ResponseCounselingReservation createReservation(User user, RequestCounselingReservation request);

  List<ResponseCounselingReservation> getMyReservations(User user);

  void cancelReservation(User user, Long id);
}
