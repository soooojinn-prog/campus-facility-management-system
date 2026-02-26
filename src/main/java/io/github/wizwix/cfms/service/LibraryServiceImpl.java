package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.response.building.*;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibraryServiceImpl implements ILibraryService {

  private final BookRepository                  bookRepository;
  private final SeatReservationRepository       seatReservationRepository;
  private final StudyRoomReservationRepository  studyRoomReservationRepository;

  // ── 열람실 스펙 정의 ──
  // TODO: ReadingRoom 엔티티 + DevLoader로 전환 (현재 하드코딩 데이터)
  // id → { totalSeats, targetUsedRate(%) }
  // targetUsedRate 는 OCCUPIED 비율 목표 — 100 절대 초과 불가
  // 2F: 29% 사용 중 (≈71% 이용가능), 3F: 44% 사용 중 (≈56% 이용가능)
  private static final Map<Long, int[]> ROOM_SPEC = Map.of(
      1L, new int[]{ 128, 29 },   // 제1열람실  2F
      2L, new int[]{  76, 44 },   // 제2열람실  3F
      3L, new int[]{  80, 15 },   // 야간열람실 B1
      4L, new int[]{  60, 92 }    // 디지털열람실 4F
  );
  private static final Map<Long, String> ROOM_NAME  = Map.of(
      1L, "제1열람실", 2L, "제2열람실", 3L, "야간열람실", 4L, "디지털열람실");
  private static final Map<Long, String> ROOM_FLOOR = Map.of(
      1L, "2F", 2L, "3F", 3L, "B1", 4L, "4F");

  // ── 열람실 공통 계산 헬퍼 ──
  /**
   * 오늘 날짜 기준으로 한 열람실의 좌석 상태 목록을 생성한다.
   *
   * 규칙:
   *  1. DB에서 예약된(RESERVED) 좌석 번호를 먼저 확정한다.
   *  2. 나머지 좌석을 날짜+roomId 기반 시드로 셔플한다.
   *  3. 셔플 결과 앞에서 targetOccupied 개수만큼 OCCUPIED, 나머지 AVAILABLE.
   *  4. targetOccupied = min(total × rate / 100, total - reserved) — 절대 초과 불가
   */
  private List<Map<String, Object>> buildSeats(Long roomId, LocalDate date) {
    int[] spec           = ROOM_SPEC.getOrDefault(roomId, new int[]{60, 50});
    int   total          = spec[0];
    int   targetRate     = Math.min(spec[1], 100);

    // 1. DB 예약 좌석
    Set<Integer> reservedNos = seatReservationRepository
        .findByRoomIdAndDate(roomId, date)
        .stream()
        .map(SeatReservation::getSeatNo)
        .collect(Collectors.toSet());

    int reserved         = reservedNos.size();
    // 2. OCCUPIED 개수 — (total - reserved) 를 절대 초과 불가
    int targetOccupied   = Math.min((int)(total * targetRate / 100.0), total - reserved);

    // 3. 예약 안 된 좌석 목록 셔플 (시드: roomId + 날짜 day-of-year)
    List<Integer> freeSeats = new ArrayList<>();
    for (int i = 1; i <= total; i++) {
      if (!reservedNos.contains(i)) freeSeats.add(i);
    }
    long seed = roomId * 10000L + date.getDayOfYear();
    Collections.shuffle(freeSeats, new Random(seed));

    // 4. 앞 targetOccupied 개 → OCCUPIED, 나머지 → AVAILABLE
    Set<Integer> occupiedNos = new HashSet<>(
        freeSeats.subList(0, Math.min(targetOccupied, freeSeats.size()))
    );

    // 5. 좌석 목록 생성 (1번~total번 순서)
    List<Map<String, Object>> seats = new ArrayList<>();
    for (int seatNo = 1; seatNo <= total; seatNo++) {
      String status;
      if      (reservedNos.contains(seatNo))  status = "RESERVED";
      else if (occupiedNos.contains(seatNo))  status = "OCCUPIED";
      else                                    status = "AVAILABLE";
      seats.add(Map.of("seatNo", seatNo, "status", status));
    }
    return seats;
  }


  // ── 1. 열람실 ──

  @Override
  public List<LibraryReadingRoomResponse> getReadingRooms(Long buildingId) {
    LocalDate today = LocalDate.now();
    List<LibraryReadingRoomResponse> result = new ArrayList<>();

    for (long roomId = 1; roomId <= 4; roomId++) {
      // buildSeats 로 정확한 OCCUPIED 수 계산 (좌측 카드와 오른쪽 완전 일치)
      List<Map<String, Object>> seats = buildSeats(roomId, today);
      int total    = ROOM_SPEC.get(roomId)[0];
      int occupied = (int) seats.stream().filter(s -> "OCCUPIED".equals(s.get("status"))).count();

      result.add(new LibraryReadingRoomResponse(
          roomId,
          ROOM_NAME.get(roomId),
          ROOM_FLOOR.get(roomId),
          total,
          occupied,   // usedSeats = 실제 OCCUPIED 수 (좌측 퍼센트 기준)
          null        // 목록 조회 시 seats null
      ));
    }
    return result;
  }

  @Override
  public LibraryReadingRoomResponse getReadingRoomSeats(Long buildingId, Long roomId) {
    LocalDate today  = LocalDate.now();
    List<Map<String, Object>> seats = buildSeats(roomId, today);

    int total    = ROOM_SPEC.getOrDefault(roomId, new int[]{60,50})[0];
    int occupied = (int) seats.stream().filter(s -> "OCCUPIED".equals(s.get("status"))).count();

    return new LibraryReadingRoomResponse(
        roomId,
        ROOM_NAME.getOrDefault(roomId, "열람실"),
        ROOM_FLOOR.getOrDefault(roomId, "?F"),
        total,
        occupied,   // getReadingRooms 와 동일한 값 → 좌측/우측 완전 일치
        seats       // 좌석 배치도 포함
    );
  }

  @Override
  @Transactional
  public void reserveSeat(Long buildingId, Long roomId, Integer seatNo, String userNumber) {
    LocalDate today = LocalDate.now();

    // 1인 1좌석 제한: 같은 날 어느 열람실이든 이미 예약한 경우 거부
    if (seatReservationRepository.existsByUserNumberAndDate(userNumber, today)) {
      throw new RuntimeException("오늘 이미 열람실 좌석을 예약하셨습니다. 1인 1좌석만 예약 가능합니다.");
    }

    // 해당 좌석 이미 예약된 경우 거부
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


  // ── 2. 도서 검색 ──

  @Override
  public List<LibraryBookResponse> searchBooks(Long buildingId, String query, String publisher, String category) {
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
        .map(b -> new LibraryBookResponse(
            b.getId(), b.getTitle(), b.getAuthor(),
            b.getPublisher(), b.getCategory(),
            b.getAvailable() != null && b.getAvailable(),
            "미지정", 0
        ))
        .toList();
  }

  @Override
  public void reserveBook(Long buildingId, Long bookId) {
    // TODO: 도서 예약 엔티티 추가 후 구현
  }


  // ── 3. 스터디룸 ──
  // TODO: StudyRoom 엔티티 + DevLoader로 전환 (현재 하드코딩 데이터)
  private static final List<Map<String, Object>> STUDY_ROOM_DB = List.of(
      studyRoom(1L, "A101", "1F", 4,  List.of("TV", "화이트보드"),                          "AVAILABLE"),
      studyRoom(2L, "A102", "1F", 6,  List.of("TV", "화이트보드", "빔프로젝터"),             "AVAILABLE"),
      studyRoom(3L, "B201", "2F", 8,  List.of("화이트보드", "빔프로젝터"),                   "OCCUPIED"),
      studyRoom(4L, "B202", "2F", 4,  List.of("TV"),                                        "AVAILABLE"),
      studyRoom(5L, "C301", "3F", 10, List.of("TV", "화이트보드", "빔프로젝터", "화상회의"), "MAINTENANCE")
  );

  private static Map<String, Object> studyRoom(Long id, String name, String floor, int capacity,
                                               List<String> amenities, String status) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", id); m.put("name", name); m.put("floor", floor);
    m.put("capacity", capacity); m.put("amenities", amenities); m.put("status", status);
    return m;
  }

  @Override
  public List<LibraryStudyRoomResponse> getStudyRooms(Long buildingId) {
    return STUDY_ROOM_DB.stream()
        .map(r -> new LibraryStudyRoomResponse(
            (Long) r.get("id"), r.get("name").toString(), r.get("floor").toString(),
            (int) r.get("capacity"), (List<String>) r.get("amenities"),
            r.get("status").toString(), null
        ))
        .toList();
  }

  @Override
  public LibraryStudyRoomResponse getStudyRoomSlots(Long buildingId, Long roomId, String date) {
    LocalDate localDate = LocalDate.parse(date);

    // DB에서 해당 방의 예약된 시간 조회
    List<String> occupiedSlots = studyRoomReservationRepository
        .findByRoomIdAndDate(roomId, localDate)
        .stream()
        .map(r -> String.format("%02d:00", r.getStartHour()))
        .toList();

    return STUDY_ROOM_DB.stream()
        .filter(r -> r.get("id").equals(roomId))
        .findFirst()
        .map(r -> new LibraryStudyRoomResponse(
            (Long) r.get("id"), r.get("name").toString(), r.get("floor").toString(),
            (int) r.get("capacity"), (List<String>) r.get("amenities"),
            r.get("status").toString(), occupiedSlots
        ))
        .orElseThrow(() -> new RuntimeException("스터디룸을 찾을 수 없습니다: " + roomId));
  }

  @Override
  @Transactional
  public void reserveStudyRoom(Long buildingId, Long roomId, String date,
                               Integer startHour, String userNumber) {
    LocalDate localDate = LocalDate.parse(date);

    // 같은 유저가 같은 날 같은 시간에 이미 다른 방을 예약했는지 확인 (시간 중복 방지)
    if (studyRoomReservationRepository
        .existsByUserNumberAndDateAndStartHour(userNumber, localDate, startHour)) {
      throw new RuntimeException(
          String.format("동일한 시간에 예약하셨습니다. %02d:00 시간대는 이미 다른 스터디룸을 예약하셨습니다. 같은 시간에 중복 예약은 불가능합니다.", startHour)
      );
    }

    // 해당 방 + 시간 이미 다른 사람이 예약한 경우
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


  // ── 4. 혼잡도 ──
  // TODO: 실시간 혼잡도 데이터 연동 (현재 하드코딩 데이터)
  @Override
  public LibraryCongestionResponse getCongestion(Long buildingId) {
    List<Map<String, Object>> floors = List.of(
        Map.of("name", "1층 로비·안내데스크", "rate", 45, "capacity",  50),
        Map.of("name", "2층 제1열람실",       "rate", 29, "capacity", 128),
        Map.of("name", "3층 제2열람실",       "rate", 44, "capacity",  76),
        Map.of("name", "4층 디지털열람실",    "rate", 92, "capacity",  60),
        Map.of("name", "B1 야간열람실",       "rate", 15, "capacity",  80)
    );
    List<Map<String, Object>> hourly = List.of(
        Map.of("hour", "08", "rate", 10), Map.of("hour", "09", "rate", 35),
        Map.of("hour", "10", "rate", 58), Map.of("hour", "11", "rate", 72),
        Map.of("hour", "12", "rate", 65), Map.of("hour", "13", "rate", 55),
        Map.of("hour", "14", "rate", 78), Map.of("hour", "15", "rate", 82),
        Map.of("hour", "16", "rate", 75), Map.of("hour", "17", "rate", 60),
        Map.of("hour", "18", "rate", 42), Map.of("hour", "19", "rate", 30),
        Map.of("hour", "20", "rate", 20), Map.of("hour", "21", "rate", 15)
    );
    return new LibraryCongestionResponse(54, floors, hourly);
  }

  // ── 5. 공지사항 ──
  // TODO: Notice 엔티티 + DevLoader로 전환 (현재 하드코딩 데이터)
  private static final List<Map<String, Object>> NOTICE_DB = List.of(
      notice(1L, "공지",   "2025년 1학기 도서관 운영시간 변경 안내",       "2025-02-20", 1823,
          "2025년 1학기부터 평일 운영시간이 오전 7시 30분~오후 11시로 변경됩니다. 토요일은 오전 9시~오후 6시, 일요일 및 공휴일은 오전 11시~오후 5시로 운영됩니다."),
      notice(2L, "긴급",   "[긴급] 4층 디지털열람실 PC 시스템 점검 예정",  "2025-02-19",  942,
          "2025년 2월 22일(토) 오전 9시~오후 1시 4층 디지털열람실 PC 전체 시스템 정기 점검을 실시합니다."),
      notice(3L, "이벤트", "도서관 독서 마라톤 참가자 모집",               "2025-02-15", 2341,
          "2025년 봄학기 독서 마라톤 행사에 참여하세요! 3월 1일부터 5월 31일까지 10권 이상 읽은 참가자에게는 소정의 상품을 드립니다."),
      notice(4L, "공지",   "2025년 상호대차 및 원문복사 서비스 안내",      "2025-02-10",  567,
          "타 대학 소장 자료 신청 서비스(상호대차)와 원문복사 서비스를 이용하실 수 있습니다."),
      notice(5L, "안내",   "도서 연체 자동반납기 설치 완료",               "2025-02-05", 1204,
          "도서관 1층 입구에 자동반납기가 새롭게 설치되었습니다. 운영시간 외에도 24시간 도서 반납이 가능합니다.")
  );

  private static Map<String, Object> notice(Long id, String type, String title,
                                            String date, int views, String content) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", id); m.put("type", type); m.put("title", title);
    m.put("date", date); m.put("views", views); m.put("content", content);
    return m;
  }

  @Override
  public List<LibraryNoticeResponse> getNotices(Long buildingId) {
    return NOTICE_DB.stream()
        .map(n -> new LibraryNoticeResponse(
            (Long) n.get("id"), n.get("type").toString(), n.get("title").toString(),
            n.get("date").toString(), (int) n.get("views"), null
        ))
        .toList();
  }

  @Override
  public LibraryNoticeResponse getNotice(Long buildingId, Long noticeId) {
    return NOTICE_DB.stream()
        .filter(n -> n.get("id").equals(noticeId))
        .findFirst()
        .map(n -> new LibraryNoticeResponse(
            (Long) n.get("id"), n.get("type").toString(), n.get("title").toString(),
            n.get("date").toString(), (int) n.get("views"), n.get("content").toString()
        ))
        .orElseThrow(() -> new RuntimeException("공지를 찾을 수 없습니다: " + noticeId));
  }
}
