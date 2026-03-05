package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.request.dorm.RequestDormApply;
import io.github.wizwix.cfms.dto.response.dorm.ResponseDormApplyResult;
import io.github.wizwix.cfms.dto.response.dorm.ResponseDormFloor;
import io.github.wizwix.cfms.dto.response.dorm.ResponseDormMyApplication;
import io.github.wizwix.cfms.dto.response.dorm.ResponseDormRoom;
import io.github.wizwix.cfms.exception.NotFoundException;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.dorm.DormApplication;
import io.github.wizwix.cfms.model.dorm.DormRoom;
import io.github.wizwix.cfms.model.enums.DormApplicationStatus;
import io.github.wizwix.cfms.model.enums.Gender;
import io.github.wizwix.cfms.model.enums.UserRole;
import io.github.wizwix.cfms.repo.UserRepository;
import io.github.wizwix.cfms.repo.dorm.DormApplicationRepository;
import io.github.wizwix.cfms.repo.dorm.DormRoomRepository;
import io.github.wizwix.cfms.service.iface.IDormService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class DormService implements IDormService {
  private final DormApplicationRepository dormAppRepo;
  private final DormRoomRepository dormRoomRepo;
  private final UserRepository userRepo;

  @Override
  public ResponseDormApplyResult apply(String userNumber, RequestDormApply request) {
    User user = userRepo.findByNumber(userNumber).orElseThrow(() -> new NotFoundException("존재하지 않는 호실입니다."));

    if (user.getRole() != UserRole.ROLE_STUDENT) {
      throw new IllegalArgumentException("학생만 기숙사 신청이 가능합니다.");
    }

    DormRoom room = dormRoomRepo.findById(request.roomId()).orElseThrow(() -> new NotFoundException("존재하지 않는 호실입니다."));

    // 성별 일치 검사
    if (user.getGender() != room.getGender()) {
      throw new IllegalArgumentException("본인의 성별에 맞는 기숙사만 신청할 수 있습니다.");
    }

    String semester = request.semester();

    // 중복 신청 검사
    List<DormApplicationStatus> activeStatuses = List.of(DormApplicationStatus.PENDING, DormApplicationStatus.APPROVED);
    if (dormAppRepo.existsByApplicantAndSemesterAndStatusIn(user, semester, activeStatuses)) {
      throw new IllegalArgumentException("이미 해당 학기에 기숙사를 신청하셨습니다.");
    }

    // 빈 자리 검사
    List<DormApplication> roomApps = dormAppRepo.findByRoomInAndSemesterAndStatusIn(List.of(room), semester, activeStatuses);
    int currentOccupancy = 0;
    for (DormApplication app : roomApps) {
      currentOccupancy += 1 + (app.getPartner() != null ? 1 : 0);
    }

    // partner 처리
    User partner = null;
    if (request.partnerNumber() != null && !request.partnerNumber().isBlank()) {
      partner = userRepo.findByNumberAndEnabledTrue(request.partnerNumber()).orElseThrow(() -> new IllegalArgumentException("같이 신청할 인원의 학번이 존재하지 않습니다."));
      if (partner.getId().equals(user.getId())) {
        throw new IllegalArgumentException("본인의 학번은 입력할 수 없습니다.");
      }
      if (partner.getRole() != UserRole.ROLE_STUDENT) {
        throw new IllegalArgumentException("학생만 기숙사 신청이 가능합니다.");
      }
      if (partner.getGender() != room.getGender()) {
        throw new IllegalArgumentException("같이 신청하는 친구의 성별이 해당 기숙사와 일치하지 않습니다.");
      }
      // 친구도 중복 신청 검사
      if (dormAppRepo.existsByApplicantAndSemesterAndStatusIn(partner, semester, activeStatuses)) {
        throw new IllegalArgumentException("친구(" + partner.getName() + ")가 이미 해당 학기에 기숙사를 신청했습니다.");
      }
      // 같이 신청이면 2자리 필요
      if (currentOccupancy + 2 > 2) {
        throw new IllegalArgumentException("해당 호실에 2명이 함께 입주할 빈 자리가 없습니다.");
      }
    } else {
      if (currentOccupancy + 1 > 2) {
        throw new IllegalArgumentException("해당 호실에 빈 자리가 없습니다.");
      }
    }

    DormApplication application = new DormApplication();
    application.setRoom(room);
    application.setApplicant(user);
    application.setPartner(partner);
    application.setSemester(semester);
    application.setPeriod(request.period());
    application.setStatus(DormApplicationStatus.PENDING);
    application.setCreatedAt(LocalDateTime.now());
    dormAppRepo.save(application);

    String message = partner != null ? room.getRoomNumber() + "호에 " + user.getName() + ", " + partner.getName() + " 같이 신청이 완료되었습니다." : room.getRoomNumber() + "호에 신청이 완료되었습니다.";

    return new ResponseDormApplyResult(application.getId(), room.getRoomNumber(), message);
  }

  /// 기숙사 신청 취소 — PENDING만 가능
  /// 본인 신청인지 확인 후, status를 CANCELLED로 변경 (DB 삭제 아님)
  @Override
  public void cancelApplication(String number, Long applicationId) {
    User user = userRepo.findByNumber(number).orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));
    DormApplication app = dormAppRepo.findById(applicationId).orElseThrow(() -> new NotFoundException("신청 내역을 찾을 수 없습니다."));
    if (!app.getApplicant().getId().equals(user.getId())) {
      throw new IllegalArgumentException("본인의 신청만 취소할 수 있습니다.");
    }
    if (app.getStatus() != DormApplicationStatus.PENDING) {
      throw new IllegalArgumentException("대기 중인 신청만 취소할 수 있습니다.");
    }
    app.setStatus(DormApplicationStatus.CANCELLED);
    dormAppRepo.save(app);
  }

  /// 관리자 — 상태별 기숙사 신청 목록 조회
  @Override
  @Transactional(readOnly = true)
  public List<ResponseDormMyApplication> getDormApplicationsByStatus(DormApplicationStatus status) {
    return dormAppRepo.findByStatus(status).stream().map(app -> new ResponseDormMyApplication(app.getId(), app.getRoom().getRoomNumber(), app.getSemester(), app.getPeriod(), app.getStatus(), app.getApplicant().getName() + (app.getPartner() != null ? "," + app.getPartner().getName() : ""), app.getRejectReason(), app.getCreatedAt())).toList();
  }

  @Override
  public List<ResponseDormFloor> getDormRooms(Gender gender) {
    List<DormRoom> rooms = dormRoomRepo.findByGender(gender);
    String semester = currentSemester();

    // 승인된 신청 기준으로 각 호실별 입주 인원 계산
    List<DormApplicationStatus> activeStatuses = List.of(DormApplicationStatus.PENDING, DormApplicationStatus.APPROVED);
    List<DormApplication> applications = rooms.isEmpty() ? List.of() : dormAppRepo.findByRoomInAndSemesterAndStatusIn(rooms, semester, activeStatuses);

    // roomId → 입주 인원 수 및 입주자 이름
    Map<Long, Integer> occupancyMap = new HashMap<>();
    Map<Long, String> residentNameMap = new HashMap<>();
    for (DormApplication app : applications) {
      Long roomId = app.getRoom().getId();
      int count = occupancyMap.getOrDefault(roomId, 0);
      // 신청자 1명 + partner가 있으면 +1
      int appCount = 1 + (app.getPartner() != null ? 1 : 0);
      occupancyMap.put(roomId, count + appCount);
      if (count == 0) {
        residentNameMap.put(roomId, maskName(app.getApplicant().getName()));
      }
    }

    // 층별 그룹핑
    Map<Integer, List<ResponseDormRoom>> floorMap = new HashMap<>();
    for (DormRoom room : rooms) {
      int occ = Math.min(occupancyMap.getOrDefault(room.getId(), 0), 2);
      String resident = occ == 1 ? residentNameMap.get(room.getId()) : null;
      ResponseDormRoom dto = new ResponseDormRoom(room.getId(), room.getRoomNumber(), room.getFloor(), occ, resident);
      floorMap.computeIfAbsent(room.getFloor(), k -> new ArrayList<>()).add(dto);
    }

    return floorMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e -> new ResponseDormFloor(e.getKey(), e.getValue())).toList();
  }

  /// 현재 학기 문자열 (간단히 연도-학기로 생성)
  private String currentSemester() {
    int year = LocalDateTime.now().getYear();
    int month = LocalDateTime.now().getMonthValue();
    return year + "-" + (month <= 7 ? "1" : "2");
  }

  /// 내 기숙사 신청 내역 — 마이페이지 기숙사 탭
  /// CANCELLED 상태는 프론트에서 취소 시 즉시 상태 변경하므로, 조회 목록에서는 제외
  /// partner가 있으면 이름을 그대로 반환 (마스킹 없음 — 본인 신청이므로)
  @Override
  @Transactional(readOnly = true)
  public List<ResponseDormMyApplication> getMyApplications(String userNumber) {
    User user = userRepo.findByNumber(userNumber).orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));
    List<DormApplicationStatus> visibleStatuses = List.of(DormApplicationStatus.PENDING, DormApplicationStatus.APPROVED, DormApplicationStatus.REJECTED);
    List<DormApplication> apps = dormAppRepo.findByApplicantAndStatusIn(user, visibleStatuses);
    return apps.stream().map(app -> new ResponseDormMyApplication(app.getId(), app.getRoom().getRoomNumber(), app.getSemester(), app.getPeriod(), app.getStatus(), app.getApplicant().getName() + (app.getPartner() != null ? "," + app.getPartner().getName() : ""), app.getRejectReason(), app.getCreatedAt())).toList();
  }

  /// 관리자 — 기숙사 신청 승인/거절 (PENDING만 처리 가능)
  @Override
  public void updateDormApplicationStatus(Long id, DormApplicationStatus status, String rejectReason, String adminNumber) {
    DormApplication app = dormAppRepo.findById(id).orElseThrow(() -> new NotFoundException("신청 내역을 찾을 수 없습니다."));
    if (app.getStatus() != DormApplicationStatus.PENDING) {
      throw new IllegalArgumentException("대기 중인 신청만 처리할 수 있습니다.");
    }
    User admin = userRepo.findByNumber(adminNumber).orElseThrow(() -> new NotFoundException("관리자를 찾을 수 없습니다."));
    app.setStatus(status);
    app.setRejectReason(status == DormApplicationStatus.REJECTED ? rejectReason : null);
    app.setProcessedBy(admin);
    app.setProcessedAt(LocalDateTime.now());
    dormAppRepo.save(app);
  }

  /// 이름 가운데 글자를 ○로 마스킹 (개인정보 보호)
  private String maskName(String name) {
    if (name == null || name.length() < 2) return name;
    if (name.length() == 2) return name.charAt(0) + "○";
    return name.charAt(0) + "○" + name.substring(2);
  }
}
