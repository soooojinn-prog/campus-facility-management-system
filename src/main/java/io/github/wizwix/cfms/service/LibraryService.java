package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.response.building.ResponseLibraryBook;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryCongestion;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryNotice;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryReadingRoom;
import io.github.wizwix.cfms.dto.response.building.ResponseLibraryStudyRoom;
import io.github.wizwix.cfms.model.SeatReservation;
import io.github.wizwix.cfms.model.StudyRoomReservation;
import io.github.wizwix.cfms.repo.BookRepository;
import io.github.wizwix.cfms.repo.SeatReservationRepository;
import io.github.wizwix.cfms.repo.StudyRoomReservationRepository;
import io.github.wizwix.cfms.service.iface.ILibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

  // ── 5. 공지사항 ──
  private static final List<Map<String, Object>> NOTICE_DB = List.of(
      notice(1L, "공지", "2025년 1학기 도서관 운영시간 변경 안내", "2025-02-20", 1823,
          "2025년 1학기부터 평일 운영시간이 오전 7시 30분~오후 11시로 변경됩니다. 토요일은 오전 9시~오후 6시, 일요일 및 공휴일은 오전 11시~오후 5시로 운영됩니다."),
      notice(2L, "긴급", "[긴급] 4층 디지털열람실 PC 시스템 점검 예정", "2025-02-19", 942,
          "2025년 2월 22일(토) 오전 9시~오후 1시 4층 디지털열람실 PC 전체 시스템 정기 점검을 실시합니다."),
      notice(3L, "이벤트", "도서관 독서 마라톤 참가자 모집", "2025-02-15", 2341,
          "2025년 봄학기 독서 마라톤 행사에 참여하세요! 3월 1일부터 5월 31일까지 10권 이상 읽은 참가자에게는 소정의 상품을 드립니다."),
      notice(4L, "공지", "2025년 상호대차 및 원문복사 서비스 안내", "2025-02-10", 567,
          "타 대학 소장 자료 신청 서비스(상호대차)와 원문복사 서비스를 이용하실 수 있습니다."),
      notice(5L, "안내", "도서 연체 자동반납기 설치 완료", "2025-02-05", 1204,
          "도서관 1층 입구에 자동반납기가 새롭게 설치되었습니다. 운영시간 외에도 24시간 도서 반납이 가능합니다.")
  );

  private static final Map<Long, String> ROOM_FLOOR = Map.of(
      1L, "2F", 2L, "3F", 3L, "B1", 4L, "4F");

  private static final Map<Long, String> ROOM_NAME = Map.of(
      1L, "제1열람실", 2L, "제2열람실", 3L, "야간열람실", 4L, "디지털열람실");

  // id → { totalSeats, targetUsedRate(%) }
  private static final Map<Long, int[]> ROOM_SPEC = Map.of(
      1L, new int[]{128, 29},   // 제1열람실  2F
      2L, new int[]{76, 44},    // 제2열람실  3F
      3L, new int[]{80, 15},    // 야간열람실 B1
      4L, new int[]{60, 92}     // 디지털열람실 4F
  );

  // ── 3. 스터디룸 ──
  private static final List<Map<String, Object>> STUDY_ROOM_DB = List.of(
      studyRoom(1L, "A101", "1F", 4, List.of("TV", "화이트보드"), "AVAILABLE"),
      studyRoom(2L, "A102", "1F", 6, List.of("TV", "화이트보드", "빔프로젝터"), "AVAILABLE"),
      studyRoom(3L, "B201", "2F", 8, List.of("화이트보드", "빔프로젝터"), "OCCUPIED"),
      studyRoom(4L, "B202", "2F", 4, List.of("TV"), "AVAILABLE"),
      studyRoom(5L, "C301", "3F", 10, List.of("TV", "화이트보드", "빔프로젝터", "화상회의"), "MAINTENANCE")
  );

  private final BookRepository bookRepository;
  private final SeatReservationRepository seatReservationRepository;
  private final StudyRoomReservationRepository studyRoomReservationRepository;

  private static Map<String, Object> notice(Long id, String type, String title,
                                            String date, int views, String content) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", id);
    m.put("type", type);
    m.put("title", title);
    m.put("date", date);
    m.put("views", views);
    m.put("content", content);
    return m;
  }

  private static Map<String, Object> studyRoom(Long id, String name, String floor, int capacity,
                                               List<String> amenities, String status) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", id);
    m.put("name", name);
    m.put("floor", floor);
    m.put("capacity", capacity);
    m.put("amenities", amenities);
    m.put("status", status);
    return m;
  }

  // ── 4. 혼잡도 ──
  @Override
  public ResponseLibraryCongestion getCongestion(Long buildingId) {
    int[] spec4F = ROOM_SPEC.get(4L); int rate4F = spec4F[1];
    int[] spec3F = ROOM_SPEC.get(2L); int rate3F = spec3F[1];
    int[] spec2F = ROOM_SPEC.get(1L); int rate2F = spec2F[1];
    int[] specB1 = ROOM_SPEC.get(3L); int rateB1 = specB1[1];

    List<Map<String, Object>> floors = new ArrayList<>(List.of(
        new LinkedHashMap<>(Map.of("name", "4층 디지털열람실",    "rate", rate4F, "capacity",  60)),
        new LinkedHashMap<>(Map.of("name", "3층 제2열람실",       "rate", rate3F, "capacity",  76)),
        new LinkedHashMap<>(Map.of("name", "2층 제1열람실",       "rate", rate2F, "capacity", 128)),
        new LinkedHashMap<>(Map.of("name", "1층 로비·안내데스크", "rate", 45,     "capacity",  50)),
        new LinkedHashMap<>(Map.of("name", "B1 야간열람실",       "rate", rateB1, "capacity",  80))
    ));

    List<Map<String, Object>> hourly = List.of(
        Map.of("hour", "08", "rate", 10), Map.of("hour", "09", "rate", 35),
        Map.of("hour", "10", "rate", 58), Map.of("hour", "11", "rate", 72),
        Map.of("hour", "12", "rate", 65), Map.of("hour", "13", "rate", 55),
        Map.of("hour", "14", "rate", 78), Map.of("hour", "15", "rate", 82),
        Map.of("hour", "16", "rate", 75), Map.of("hour", "17", "rate", 60),
        Map.of("hour", "18", "rate", 42), Map.of("hour", "19", "rate", 30),
        Map.of("hour", "20", "rate", 20), Map.of("hour", "21", "rate", 15)
    );

    int overallRate = floors.stream().mapToInt(f -> (int) f.get("rate")).sum() / floors.size();
    return new ResponseLibraryCongestion(floors, hourly, overallRate);
  }

  // ── 6. 내 예약 조회 (마이페이지) ──
  @Override
  public List<Map<String, Object>> getMySeatReservations(String userNumber) {
    return seatReservationRepository.findAll().stream()
        .filter(r -> userNumber.equals(r.getUserNumber()))
        .sorted(java.util.Comparator.comparing(SeatReservation::getDate).reversed())
        .map(r -> {
          Map<String, Object> m = new LinkedHashMap<>();
          m.put("id",       r.getId());
          m.put("roomName", ROOM_NAME.getOrDefault(r.getRoomId(), "열람실"));
          m.put("floor",    ROOM_FLOOR.getOrDefault(r.getRoomId(), "?F"));
          m.put("seatNo",   r.getSeatNo());
          m.put("date",     r.getDate().toString());
          return m;
        })
        .toList();
  }

  @Override
  public List<Map<String, Object>> getMyStudyRoomReservations(String userNumber) {
    return studyRoomReservationRepository.findAll().stream()
        .filter(r -> userNumber.equals(r.getUserNumber()))
        .sorted(java.util.Comparator.comparing(StudyRoomReservation::getDate).reversed())
        .map(r -> {
          String roomName = STUDY_ROOM_DB.stream()
              .filter(s -> s.get("id").equals(r.getRoomId()))
              .map(s -> (String) s.get("name"))
              .findFirst().orElse("스터디룸");
          String floor = STUDY_ROOM_DB.stream()
              .filter(s -> s.get("id").equals(r.getRoomId()))
              .map(s -> (String) s.get("floor"))
              .findFirst().orElse("?F");
          Map<String, Object> m = new LinkedHashMap<>();
          m.put("id",        r.getId());
          m.put("roomName",  roomName);
          m.put("floor",     floor);
          m.put("date",      r.getDate().toString());
          m.put("startHour", r.getStartHour());
          m.put("endHour",   r.getStartHour() + 1);
          return m;
        })
        .toList();
  }

  // ── 5. 공지사항 ──
  @Override
  public ResponseLibraryNotice getNotice(Long buildingId, Long noticeId) {
    return NOTICE_DB.stream()
        .filter(n -> n.get("id").equals(noticeId))
        .findFirst()
        .map(n -> new ResponseLibraryNotice(
            (Long) n.get("id"), (String) n.get("content"),
            (String) n.get("date"), (String) n.get("title"),
            (String) n.get("type"), (int) n.get("views")
        ))
        .orElseThrow(() -> new RuntimeException("공지를 찾을 수 없습니다: " + noticeId));
  }

  @Override
  public List<ResponseLibraryNotice> getNotices(Long buildingId) {
    return NOTICE_DB.stream()
        .map(n -> new ResponseLibraryNotice(
            (Long) n.get("id"), null,
            (String) n.get("date"), (String) n.get("title"),
            (String) n.get("type"), (int) n.get("views")
        ))
        .toList();
  }

  // ── 1. 열람실 ──
  @Override
  public ResponseLibraryReadingRoom getReadingRoomSeats(Long buildingId, Long roomId) {
    LocalDate today = LocalDate.now();
    List<Map<String, Object>> seats = buildSeats(roomId, today);

    int total = ROOM_SPEC.getOrDefault(roomId, new int[]{60, 50})[0];
    int occupied = (int) seats.stream().filter(s -> "OCCUPIED".equals(s.get("status"))).count();

    return new ResponseLibraryReadingRoom(
        roomId,
        ROOM_NAME.getOrDefault(roomId, "열람실"),
        ROOM_FLOOR.getOrDefault(roomId, "?F"),
        seats,
        total,
        occupied
    );
  }

  @Override
  public List<ResponseLibraryReadingRoom> getReadingRooms(Long buildingId) {
    LocalDate today = LocalDate.now();
    List<ResponseLibraryReadingRoom> result = new ArrayList<>();

    for (long roomId = 1; roomId <= 4; roomId++) {
      List<Map<String, Object>> seats = buildSeats(roomId, today);
      int total = ROOM_SPEC.get(roomId)[0];
      int occupied = (int) seats.stream().filter(s -> "OCCUPIED".equals(s.get("status"))).count();

      result.add(new ResponseLibraryReadingRoom(
          roomId,
          ROOM_NAME.get(roomId),
          ROOM_FLOOR.get(roomId),
          null,
          total,
          occupied
      ));
    }
    return result;
  }

  private List<Map<String, Object>> buildSeats(Long roomId, LocalDate date) {
    int[] spec = ROOM_SPEC.getOrDefault(roomId, new int[]{60, 50});
    int total = spec[0];
    int targetRate = Math.min(spec[1], 100);

    Set<Integer> reservedNos = seatReservationRepository
        .findByRoomIdAndDate(roomId, date)
        .stream()
        .map(SeatReservation::getSeatNo)
        .collect(Collectors.toSet());

    int reserved = reservedNos.size();
    int targetOccupied = Math.min((int) (total * targetRate / 100.0), total - reserved);

    List<Integer> freeSeats = new ArrayList<>();
    for (int i = 1; i <= total; i++) {
      if (!reservedNos.contains(i)) freeSeats.add(i);
    }

    long seed = roomId * 10000L + date.getDayOfYear();
    Collections.shuffle(freeSeats, new Random(seed));

    Set<Integer> occupiedNos = new HashSet<>(
        freeSeats.subList(0, Math.min(targetOccupied, freeSeats.size()))
    );

    List<Map<String, Object>> seats = new ArrayList<>();
    for (int seatNo = 1; seatNo <= total; seatNo++) {
      String status;
      if (reservedNos.contains(seatNo)) status = "RESERVED";
      else if (occupiedNos.contains(seatNo)) status = "OCCUPIED";
      else status = "AVAILABLE";
      seats.add(Map.of("seatNo", seatNo, "status", status));
    }
    return seats;
  }

  // ── 3. 스터디룸 ──
  @Override
  public ResponseLibraryStudyRoom getStudyRoomSlots(Long buildingId, Long roomId, String date) {
    LocalDate localDate = LocalDate.parse(date);

    List<String> occupiedSlots = studyRoomReservationRepository
        .findByRoomIdAndDate(roomId, localDate)
        .stream()
        .map(r -> String.format("%02d:00", r.getStartHour()))
        .toList();

    Map<String, Object> room = STUDY_ROOM_DB.stream()
        .filter(r -> r.get("id").equals(roomId))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("스터디룸을 찾을 수 없습니다: " + roomId));

    Long sId = (Long) room.get("id");
    String sName = (String) room.get("name");
    String sFloor = (String) room.get("floor");
    int sCapacity = (int) room.get("capacity");
    @SuppressWarnings("unchecked")
    List<String> sAmenities = (List<String>) room.get("amenities");
    String sStatus = (String) room.get("status");

    return new ResponseLibraryStudyRoom(sId, sName, sAmenities, sCapacity, sFloor, occupiedSlots, sStatus);
  }

  @Override
  public List<ResponseLibraryStudyRoom> getStudyRooms(Long buildingId) {
    List<ResponseLibraryStudyRoom> studyResult = new ArrayList<>();
    for (Map<String, Object> r : STUDY_ROOM_DB) {
      Long sId = (Long) r.get("id");
      String sName = (String) r.get("name");
      String sFloor = (String) r.get("floor");
      int sCapacity = (int) r.get("capacity");
      @SuppressWarnings("unchecked")
      List<String> sAmenities = (List<String>) r.get("amenities");
      String sStatus = (String) r.get("status");
      studyResult.add(new ResponseLibraryStudyRoom(sId, sName, sAmenities, sCapacity, sFloor, null, sStatus));
    }
    return studyResult;
  }

  // ── 예약 ──
  @Override
  public void reserveBook(Long buildingId, Long bookId) {
    // TODO
  }

  @Override
  @Transactional
  public void reserveSeat(Long buildingId, Long roomId, Integer seatNo, String userNumber) {
    LocalDate today = LocalDate.now();

    if (seatReservationRepository.existsByUserNumberAndDate(userNumber, today)) {
      throw new RuntimeException("오늘 이미 열람실 좌석을 예약하셨습니다. 1인 1좌석만 예약 가능합니다.");
    }

    if (seatReservationRepository.existsByRoomIdAndSeatNoAndDate(roomId, seatNo, today)) {
      throw new RuntimeException("이미 다른 사용자가 예약한 좌석입니다.");
    }

    seatReservationRepository.save(
        SeatReservation.builder()
            .roomId(roomId)
            .seatNo(seatNo)
            .date(today)
            .userNumber(userNumber)
            .build()
    );
  }

  @Override
  @Transactional
  public void reserveStudyRoom(Long buildingId, Long roomId, String date,
                               Integer startHour, String userNumber) {
    LocalDate localDate = LocalDate.parse(date);

    if (studyRoomReservationRepository
        .existsByUserNumberAndDateAndStartHour(userNumber, localDate, startHour)) {
      throw new RuntimeException(
          String.format("동일한 시간에 예약하셨습니다. %02d:00 시간대는 이미 다른 스터디룸을 예약하셨습니다. 같은 시간에 중복 예약은 불가능합니다.", startHour)
      );
    }

    if (studyRoomReservationRepository
        .existsByRoomIdAndDateAndStartHour(roomId, localDate, startHour)) {
      throw new RuntimeException("이미 다른 사용자가 예약한 시간입니다.");
    }

    studyRoomReservationRepository.save(
        StudyRoomReservation.builder()
            .roomId(roomId)
            .date(localDate)
            .startHour(startHour)
            .userNumber(userNumber)
            .build()
    );
  }

  // ── 2. 도서 검색 ──
  @Override
  public List<ResponseLibraryBook> searchBooks(Long buildingId, String query, String publisher, String category) {
    return bookRepository.findAll().stream()
        .filter(b -> {
          boolean matchQ = query == null || query.isBlank()
              || b.getTitle().contains(query)
              || b.getAuthor().contains(query)
              || b.getPublisher().contains(query);
          boolean matchPub = publisher == null || publisher.isBlank()
              || b.getPublisher().contains(publisher);
          boolean matchCat = category == null || category.isBlank()
              || category.equals(b.getCategory());
          return matchQ && matchPub && matchCat;
        })
        .map(b -> new ResponseLibraryBook(
            b.getId(), b.getAuthor(),
            b.getAvailable() != null && b.getAvailable(),
            b.getCategory(), "미지정", b.getPublisher(), b.getTitle(), 0
        ))
        .toList();
  }

  // ── 취소 ── (컨트롤러가 LibraryService에 직접 의존하므로 그대로 두면 됨)
  public void cancelSeatReservation(String userNumber, Long reservationId) {
    SeatReservation reservation = seatReservationRepository.findById(reservationId)
        .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));
    if (!userNumber.equals(reservation.getUserNumber()))
      throw new RuntimeException("본인의 예약만 취소할 수 있습니다.");
    seatReservationRepository.delete(reservation);
  }

  public void cancelStudyRoomReservation(String userNumber, Long reservationId) {
    StudyRoomReservation reservation = studyRoomReservationRepository.findById(reservationId)
        .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다."));
    if (!userNumber.equals(reservation.getUserNumber()))
      throw new RuntimeException("본인의 예약만 취소할 수 있습니다.");
    studyRoomReservationRepository.delete(reservation);
  }
}
