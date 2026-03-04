package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.response.building.ResponseLibraryBook;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryCongestion;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryNotice;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryReadingRoom;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryStudyRoom;

import java.util.List;
import java.util.Map;

public interface ILibraryService {
  ResponseLibraryCongestion getCongestion();

  List<Map<String, Object>> getMySeatReservations(String userNumber);

  List<Map<String, Object>> getMyStudyRoomReservations(String userNumber);

  ResponseLibraryNotice getNotice(Long noticeId);

  List<ResponseLibraryNotice> getNotices();

  ResponseLibraryReadingRoom getReadingRoomSeats(Long roomId);

  List<ResponseLibraryReadingRoom> getReadingRooms();

  ResponseLibraryStudyRoom getStudyRoomSlots(Long roomId, String date);

  List<ResponseLibraryStudyRoom> getStudyRooms();

  void reserveBook(Long bookId, String userNumber);

  void reserveSeat(Long roomId, Integer seatNo, String userNumber);

  void reserveStudyRoom(Long roomId, String date, Integer startHour, String userNumber);

  List<ResponseLibraryBook> searchBooks(String q, String publisher, String category);

  void cancelSeatReservation(String userNumber, Long reservationId);

  void cancelStudyRoomReservation(String userNumber, Long reservationId);
}
