package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.response.building.ResponseLibraryBook;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryCongestion;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryNotice;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryReadingRoom;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryStudyRoom;

import java.util.List;
import java.util.Map;

public interface ILibraryService {

  // 1. 열람실
  List<ResponseLibraryReadingRoom> getReadingRooms(Long buildingId);

  ResponseLibraryStudyRoom getStudyRoomSlots(Long buildingId, Long roomId, String date);

  // 2. 도서 검색
  List<ResponseLibraryBook> searchBooks(Long buildingId, String query, String publisher, String category);

  // 3. 스터디룸
  List<ResponseLibraryStudyRoom> getStudyRooms(Long buildingId);

  void reserveBook(Long buildingId, Long bookId);

  void reserveSeat(Long buildingId, Long roomId, Integer seatNo, String userNumber);

  void reserveStudyRoom(Long buildingId, Long roomId, String date, Integer startHour, String userNumber);

  // 4. 혼잡도
  ResponseLibraryCongestion getCongestion(Long buildingId);

  // 5. 공지사항
  List<ResponseLibraryNotice> getNotices(Long buildingId);

  ResponseLibraryReadingRoom getReadingRoomSeats(Long buildingId, Long roomId);

  // 6. 내 예약 조회 (마이페이지용)
  List<Map<String, Object>> getMySeatReservations(String userNumber);

  List<Map<String, Object>> getMyStudyRoomReservations(String userNumber);

  ResponseLibraryNotice getNotice(Long buildingId, Long noticeId);

}
