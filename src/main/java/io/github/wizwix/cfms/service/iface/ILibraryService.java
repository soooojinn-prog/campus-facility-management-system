package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.response.building.LibraryBookResponse;
import io.github.wizwix.cfms.dto.response.building.LibraryCongestionResponse;
import io.github.wizwix.cfms.dto.response.building.LibraryNoticeResponse;
import io.github.wizwix.cfms.dto.response.building.LibraryReadingRoomResponse;
import io.github.wizwix.cfms.dto.response.building.LibraryStudyRoomResponse;

import java.util.List;
import java.util.Map;

public interface ILibraryService {

  // 1. 열람실
  List<LibraryReadingRoomResponse> getReadingRooms(Long buildingId);
  LibraryReadingRoomResponse getReadingRoomSeats(Long buildingId, Long roomId);
  void reserveSeat(Long buildingId, Long roomId, Integer seatNo, String userNumber);

  // 2. 도서 검색
  List<LibraryBookResponse> searchBooks(Long buildingId, String query, String publisher, String category);
  void reserveBook(Long buildingId, Long bookId);

  // 3. 스터디룸
  List<LibraryStudyRoomResponse> getStudyRooms(Long buildingId);
  LibraryStudyRoomResponse getStudyRoomSlots(Long buildingId, Long roomId, String date);
  void reserveStudyRoom(Long buildingId, Long roomId, String date, Integer startHour, String userNumber);

  // 4. 혼잡도
  LibraryCongestionResponse getCongestion(Long buildingId);

  // 5. 공지사항
  List<LibraryNoticeResponse> getNotices(Long buildingId);
  LibraryNoticeResponse getNotice(Long buildingId, Long noticeId);
}
