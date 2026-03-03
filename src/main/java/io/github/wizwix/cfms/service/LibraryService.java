package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.response.building.ResponseLibraryBook;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryCongestion;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryNotice;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryReadingRoom;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryStudyRoom;
import io.github.wizwix.cfms.exception.DuplicatedReservationException;
import io.github.wizwix.cfms.exception.NotAvailableException;
import io.github.wizwix.cfms.exception.NotFoundException;
import io.github.wizwix.cfms.model.library.LibraryBookReservation;
import io.github.wizwix.cfms.model.library.LibraryReadingRoom;
import io.github.wizwix.cfms.model.library.LibrarySeatReservation;
import io.github.wizwix.cfms.model.library.LibraryStudyRoom;
import io.github.wizwix.cfms.model.library.LibraryStudyRoomReservation;
import io.github.wizwix.cfms.repo.library.LibraryBookRepository;
import io.github.wizwix.cfms.repo.library.LibraryBookReservationRepository;
import io.github.wizwix.cfms.repo.library.LibraryNoticeRepository;
import io.github.wizwix.cfms.repo.library.LibraryReadingRoomRepository;
import io.github.wizwix.cfms.repo.library.LibrarySeatReservationRepository;
import io.github.wizwix.cfms.repo.library.LibraryStudyRoomRepository;
import io.github.wizwix.cfms.repo.library.LibraryStudyRoomReservationRepository;
import io.github.wizwix.cfms.service.iface.ILibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibraryService implements ILibraryService {
  // 시간대별 혼잡도 추이 — 통계 기반 고정값 (TODO: 센서/로그 연동)
  private static final List<Map<String, Object>> HOURLY_TREND = List.of(Map.of("hour", "08", "rate", 10), Map.of("hour", "09", "rate", 35), Map.of("hour", "10", "rate", 58), Map.of("hour", "11", "rate", 72), Map.of("hour", "12", "rate", 65), Map.of("hour", "13", "rate", 55), Map.of("hour", "14", "rate", 78), Map.of("hour", "15", "rate", 82), Map.of("hour", "16", "rate", 75), Map.of("hour", "17", "rate", 60), Map.of("hour", "18", "rate", 42), Map.of("hour", "19", "rate", 30), Map.of("hour", "20", "rate", 20), Map.of("hour", "21", "rate", 15));
  private static final int LOBBY_CAPACITY = 50;
  // ── 1F 로비: 실제 예약 좌석 없이 고정 비율 사용 (TODO: 센서 연동)
  private static final String LOBBY_NAME = "1층 로비·안내데스크";
  private static final int LOBBY_RATE = 45;
  private final LibraryBookRepository bookRepo;
  private final LibraryBookReservationRepository bookResRepo;
  private final LibraryNoticeRepository noticeRepo;
  private final LibraryReadingRoomRepository readingRoomRepo;
  private final LibrarySeatReservationRepository seatResRepo;
  private final LibraryStudyRoomReservationRepository studyResRepo;
  private final LibraryStudyRoomRepository studyRoomRepo;

  // ── 1. 혼잡도 ──

  @Override
  public ResponseLibraryCongestion getCongestion() {
    LocalDate today = LocalDate.now();
    List<LibraryReadingRoom> rooms = readingRoomRepo.findAll();

    // 층별 혼잡도: 높은 층 → 낮은 층 → B1 순서 (enum 역순)
    List<Map<String, Object>> floors = new ArrayList<>();
    rooms.stream().sorted((a, b) -> b.getFloor().compareTo(a.getFloor())).forEach(room -> {
      List<Map<String, Object>> seats = buildSeats(room.getId(), today, room.getTotalSeats(), room.getUsedRate());
      int occupied = (int) seats.stream().filter(s -> "OCCUPIED".equals(s.get("status"))).count();
      int rate = room.getTotalSeats() > 0 ? Math.min(occupied * 100 / room.getTotalSeats(), 100) : 0;
      Map<String, Object> entry = new LinkedHashMap<>();
      entry.put("name", room.getFloor().toDisplayString() + " " + room.getName());
      entry.put("rate", rate);
      entry.put("capacity", room.getTotalSeats());
      floors.add(entry);
    });

    // 1층 로비 고정값 삽입: B1 항목 앞에 삽입
    int lobbyInsertIdx = floors.size();
    for (int i = 0; i < floors.size(); i++) {
      String name = (String) floors.get(i).get("name");
      if (name.startsWith("B1")) {
        lobbyInsertIdx = i;
        break;
      }
    }
    Map<String, Object> lobby = new LinkedHashMap<>();
    lobby.put("name", LOBBY_NAME);
    lobby.put("rate", LOBBY_RATE);
    lobby.put("capacity", LOBBY_CAPACITY);
    floors.add(lobbyInsertIdx, lobby);

    int overallRate = floors.stream().mapToInt(f -> (int) f.get("rate")).sum() / floors.size();
    return new ResponseLibraryCongestion(floors, HOURLY_TREND, overallRate);
  }

  // ── 2. 공지사항 ──

  /// 오늘 날짜 기준으로 한 열람실의 좌석 상태 목록을 생성한다.
  ///
  /// 규칙:
  /// 1. DB에서 예약된(RESERVED) 좌석 번호를 먼저 확정한다.
  /// 2. 나머지 좌석을 날짜+roomId 기반 시드로 셔플한다.
  /// 3. 셔플 결과 앞에서 targetOccupied 개수만큼 OCCUPIED, 나머지 AVAILABLE.
  /// 4. targetOccupied = min(total × rate / 100, total - reserved) — 절대 초과 불가
  private List<Map<String, Object>> buildSeats(Long roomId, LocalDate date, int total, int targetRate) {
    targetRate = Math.min(targetRate, 100);

    // 1. DB 예약 좌석
    Set<Integer> reservedNos = seatResRepo.findByRoomIdAndDate(roomId, date).stream().map(LibrarySeatReservation::getSeatNo).collect(Collectors.toSet());

    int reserved = reservedNos.size();
    // 2. OCCUPIED 개수 — (total - reserved) 를 절대 초과 불가
    int targetOccupied = Math.min((int) (total * targetRate / 100.0), total - reserved);

    // 3. 예약 안 된 좌석 목록 셔플 (시드: roomId + 날짜 day-of-year)
    List<Integer> freeSeats = new ArrayList<>();
    for (int i = 1; i <= total; i++) {
      if (!reservedNos.contains(i)) freeSeats.add(i);
    }
    long seed = roomId * 10000L + date.getDayOfYear();
    Collections.shuffle(freeSeats, new Random(seed));

    // 4. 앞 targetOccupied 개 → OCCUPIED, 나머지 → AVAILABLE
    Set<Integer> occupiedNos = new HashSet<>(freeSeats.subList(0, Math.min(targetOccupied, freeSeats.size())));

    // 5. 좌석 목록 생성 (1번~total번 순서)
    List<Map<String, Object>> seats = new ArrayList<>();
    for (int seatNo = 1; seatNo <= total; seatNo++) {
      String status;
      if (reservedNos.contains(seatNo)) {status = "RESERVED";} else if (occupiedNos.contains(seatNo)) {
        status = "OCCUPIED";
      } else {status = "AVAILABLE";}
      seats.add(Map.of("seatNo", seatNo, "status", status));
    }
    return seats;
  }

  @Override
  public List<Map<String, Object>> getMySeatReservations(String userNumber) {
    return seatResRepo.findByUserNumber(userNumber).stream().sorted((a, b) -> b.getDate().compareTo(a.getDate())).map(r -> {
      LibraryReadingRoom room = readingRoomRepo.findById(r.getRoomId()).orElse(null);
      String roomName = room != null ? room.getName() : "열람실";
      String floor = room != null ? room.getFloor().toDisplayString() : "?F";
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", r.getId());
      m.put("roomName", roomName);
      m.put("floor", floor);
      m.put("seatNo", r.getSeatNo());
      m.put("date", r.getDate().toString());
      return m;
    }).toList();
  }

  // ── 3. 열람실 ──

  @Override
  public List<Map<String, Object>> getMyStudyRoomReservations(String userNumber) {
    return studyResRepo.findByUserNumber(userNumber).stream().sorted((a, b) -> b.getDate().compareTo(a.getDate())).map(r -> {
      LibraryStudyRoom room = studyRoomRepo.findById(r.getRoomId()).orElse(null);
      String roomName = room != null ? room.getName() : "스터디룸";
      String floor = room != null ? room.getFloor().toDisplayString() : "?F";
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", r.getId());
      m.put("roomName", roomName);
      m.put("floor", floor);
      m.put("date", r.getDate().toString());
      m.put("startHour", r.getStartHour());
      m.put("endHour", r.getStartHour() + 1);
      return m;
    }).toList();
  }

  @Override
  public ResponseLibraryNotice getNotice(Long noticeId) {
    return noticeRepo.findById(noticeId).map(n -> new ResponseLibraryNotice(n.getId(), n.getContent(), n.getDate().toString(), n.getTitle(), n.getType().getDisplayName(), n.getViews())).orElseThrow(() -> new NotFoundException("공지를 찾을 수 없습니다: " + noticeId));
  }

  @Override
  public List<ResponseLibraryNotice> getNotices() {
    return noticeRepo.findAll().stream().map(n -> new ResponseLibraryNotice(n.getId(), null, n.getDate().toString(), n.getTitle(), n.getType().getDisplayName(), n.getViews())).toList();
  }

  // ── 4. 스터디룸 ──

  @Override
  public ResponseLibraryReadingRoom getReadingRoomSeats(Long roomId) {
    LocalDate today = LocalDate.now();
    LibraryReadingRoom room = readingRoomRepo.findById(roomId).orElseThrow(() -> new NotFoundException("열람실을 찾을 수 없습니다: " + roomId));
    List<Map<String, Object>> seats = buildSeats(roomId, today, room.getTotalSeats(), room.getUsedRate());
    int occupied = (int) seats.stream().filter(s -> "OCCUPIED".equals(s.get("status"))).count();
    return new ResponseLibraryReadingRoom(room.getId(), room.getName(), room.getFloor().toDisplayString(), seats, room.getTotalSeats(), occupied);
  }

  @Override
  public List<ResponseLibraryReadingRoom> getReadingRooms() {
    LocalDate today = LocalDate.now();
    return readingRoomRepo.findAll().stream().map(room -> {
      List<Map<String, Object>> seats = buildSeats(room.getId(), today, room.getTotalSeats(), room.getUsedRate());
      int occupied = (int) seats.stream().filter(s -> "OCCUPIED".equals(s.get("status"))).count();
      return new ResponseLibraryReadingRoom(room.getId(), room.getName(), room.getFloor().toDisplayString(), null, room.getTotalSeats(), occupied);
    }).toList();
  }

  // ── 5. 도서 ──

  @Override
  public ResponseLibraryStudyRoom getStudyRoomSlots(Long roomId, String date) {
    LocalDate localDate = LocalDate.parse(date);
    LibraryStudyRoom room = studyRoomRepo.findById(roomId).orElseThrow(() -> new NotFoundException("스터디룸을 찾을 수 없습니다: " + roomId));

    List<String> occupiedSlots = studyResRepo.findByRoomIdAndDate(roomId, localDate).stream().map(r -> String.format("%02d:00", r.getStartHour())).toList();

    return new ResponseLibraryStudyRoom(room.getId(), room.getName(), room.getAmenities(), room.getCapacity(), room.getFloor().toDisplayString(), occupiedSlots, room.getStatus().name());
  }

  @Override
  public List<ResponseLibraryStudyRoom> getStudyRooms() {
    return studyRoomRepo.findAll().stream().map(room -> new ResponseLibraryStudyRoom(room.getId(), room.getName(), room.getAmenities(), room.getCapacity(), room.getFloor().toDisplayString(), null, room.getStatus().name())).toList();
  }

  // ── 6. 예약 ──

  @Override
  @Transactional
  public void reserveBook(Long bookId, String userNumber) {
    var book = bookRepo.findById(bookId).orElseThrow(() -> new NotFoundException("도서를 찾을 수 없습니다: " + bookId));
    if (!Boolean.TRUE.equals(book.getAvailable())) {
      throw new NotAvailableException("대출 불가능한 도서입니다.");
    }
    if (bookResRepo.existsByBookIdAndUserNumber(bookId, userNumber)) {
      throw new DuplicatedReservationException("이미 예약한 도서입니다.");
    }
    bookResRepo.save(LibraryBookReservation.builder().bookId(bookId).userNumber(userNumber).reservedAt(LocalDateTime.now()).build());
    book.setAvailable(false);
    bookRepo.save(book);
  }

  @Override
  @Transactional
  public void reserveSeat(Long roomId, Integer seatNo, String userNumber) {
    LocalDate today = LocalDate.now();

    if (!readingRoomRepo.existsById(roomId)) {
      throw new NotFoundException("열람실을 찾을 수 없습니다: " + roomId);
    }

    // 1인 1좌석 제한: 같은 날 어느 열람실이든 이미 예약한 경우 거부
    if (seatResRepo.existsByUserNumberAndDate(userNumber, today)) {
      throw new DuplicatedReservationException("오늘 이미 열람실 좌석을 예약하셨습니다. 1인 1좌석만 예약 가능합니다.");
    }

    // 해당 좌석 이미 예약된 경우 거부
    if (seatResRepo.existsByRoomIdAndSeatNoAndDate(roomId, seatNo, today)) {
      throw new DuplicatedReservationException("이미 다른 사용자가 예약한 좌석입니다.");
    }

    seatResRepo.save(LibrarySeatReservation.builder().roomId(roomId).seatNo(seatNo).date(today).userNumber(userNumber).build());
  }

  // ── 7. 마이페이지 ──

  @Override
  @Transactional
  public void reserveStudyRoom(Long roomId, String date, Integer startHour, String userNumber) {
    LocalDate localDate = LocalDate.parse(date);

    if (!studyRoomRepo.existsById(roomId)) {
      throw new NotFoundException("스터디룸을 찾을 수 없습니다: " + roomId);
    }

    // 같은 유저가 같은 날 같은 시간에 이미 다른 방을 예약했는지 확인 (시간 중복 방지)
    if (studyResRepo.existsByUserNumberAndDateAndStartHour(userNumber, localDate, startHour)) {
      throw new DuplicatedReservationException(String.format("동일한 시간에 예약하셨습니다. %02d:00 시간대는 이미 다른 스터디룸을 예약하셨습니다.", startHour));
    }

    // 해당 방 + 시간 이미 다른 사람이 예약한 경우
    if (studyResRepo.existsByRoomIdAndDateAndStartHour(roomId, localDate, startHour)) {
      throw new DuplicatedReservationException("이미 다른 사용자가 예약한 시간입니다.");
    }

    studyResRepo.save(LibraryStudyRoomReservation.builder().roomId(roomId).date(localDate).startHour(startHour).userNumber(userNumber).build());
  }

  @Override
  public List<ResponseLibraryBook> searchBooks(String query, String publisher, String category) {
    return bookRepo.findAll().stream().filter(b -> {
      boolean matchQ = query == null || query.isBlank() || b.getTitle().contains(query) || b.getAuthor().contains(query) || (b.getPublisher() != null && b.getPublisher().contains(query));
      boolean matchPub = publisher == null || publisher.isBlank() || (b.getPublisher() != null && b.getPublisher().contains(publisher));
      boolean matchCat = category == null || category.isBlank() || category.equals(b.getCategory());
      return matchQ && matchPub && matchCat;
    }).map(b -> new ResponseLibraryBook(b.getId(), b.getAuthor(), b.getAvailable() != null && b.getAvailable(), b.getCategory(), b.getLocation(), b.getPublisher(), b.getTitle(), b.getYear() != null ? b.getYear() : 0)).toList();
  }
}
