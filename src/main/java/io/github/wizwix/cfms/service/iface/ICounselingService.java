package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.request.counseling.RequestCounselingReservation;
import io.github.wizwix.cfms.dto.response.counseling.ResponseCounselingReservation;
import io.github.wizwix.cfms.dto.response.counseling.ResponseCounselor;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.enums.CounselingDepartment;

import java.time.LocalDate;
import java.util.List;

public interface ICounselingService {
  void cancelReservation(User user, Long id);

  ResponseCounselingReservation createReservation(User user, RequestCounselingReservation request);

  List<ResponseCounselor> getCounselors(CounselingDepartment dept);

  List<ResponseCounselingReservation> getMyReservations(User user);

  List<ResponseCounselingReservation> getSlots(Long counselorId, LocalDate date);
}
