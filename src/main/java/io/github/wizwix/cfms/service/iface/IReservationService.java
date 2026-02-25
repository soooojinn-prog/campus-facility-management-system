package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.request.reservation.RequestReservation;
import io.github.wizwix.cfms.dto.response.reservation.ResponseReservation;

import java.time.LocalDate;
import java.util.List;

public interface IReservationService {
  ResponseReservation createReservation(String userNumber, RequestReservation req);

  void cancelReservation(String userNumber, Long reservationId);

  List<ResponseReservation> getMyReservations(String userNumber);

  List<ResponseReservation> getRoomReservations(Long roomId, LocalDate date);
}
