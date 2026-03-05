package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.request.club.RequestClubCreate;
import io.github.wizwix.cfms.dto.request.club.RequestClubUpdate;
import io.github.wizwix.cfms.dto.response.club.ResponseClubDetail;
import io.github.wizwix.cfms.dto.response.club.ResponseClubList;
import io.github.wizwix.cfms.exception.NotFoundException;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.club.Club;
import io.github.wizwix.cfms.model.club.ClubMember;
import io.github.wizwix.cfms.model.enums.ClubMemberStatus;
import io.github.wizwix.cfms.model.enums.ClubRole;
import io.github.wizwix.cfms.model.enums.ClubStatus;
import io.github.wizwix.cfms.model.enums.UserRole;
import io.github.wizwix.cfms.repo.UserRepository;
import io.github.wizwix.cfms.repo.club.ClubMemberRepository;
import io.github.wizwix.cfms.repo.club.ClubRepository;
import io.github.wizwix.cfms.service.iface.IClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/// 동아리 관리 서비스
///
/// 동아리 CRUD, 검색, 상태 변경(관리자 승인/거절) 기능을 제공한다.
/// 프론트: ClubPage.jsx(사용자), AdminPage.jsx(관리자)에서 호출
@Service
@RequiredArgsConstructor
@Transactional
public class ClubService implements IClubService {
  private final ClubMemberRepository clubMemberRepository;
  private final ClubRepository clubRepository;
  private final UserRepository userRepository;

  /// 동아리 개설 신청 — PENDING 상태로 생성, 관리자 승인 필요
  @Override
  public ResponseClubDetail createClub(String userNumber, RequestClubCreate req) {
    User user = userRepository.findByNumber(userNumber).orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));
    Club club = new Club();
    club.setName(req.name());
    club.setSlug(req.slug());
    club.setDescription(req.description());
    club.setAutoApprove(req.autoApprove());
    club.setStatus(ClubStatus.PENDING);
    club.setPresident(user);
    club.setCreatedAt(LocalDateTime.now());
    clubRepository.save(club);
    return new ResponseClubDetail(club.getId(), club.getName(), club.getSlug(), club.getDescription(), user.getName(), club.getAutoApprove(), getMemberCount(club), club.getCreatedAt(), club.getStatus());
  }

  /// 동아리 상세 조회 — slug로 검색
  @Override
  public ResponseClubDetail getClubDetail(String slug) {
    Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("동아리를 찾을 수 없습니다."));
    return new ResponseClubDetail(club.getId(), club.getName(), club.getSlug(), club.getDescription(), getPresidentName(club), club.getAutoApprove(), getMemberCount(club), club.getCreatedAt(), club.getStatus());
  }

  /// 상태별 동아리 목록 조회 — 관리자 페이지에서 사용
  @Override
  public List<ResponseClubList> getClubListByStatus(ClubStatus status) {
    var list = new ArrayList<ResponseClubList>();
    clubRepository.findByStatus(status).forEach(c -> {
      var resp = new ResponseClubList(c.getId(), c.getName(), c.getSlug(), getPresidentName(c), c.getStatus(), getMemberCount(c), c.getCreatedAt());
      list.add(resp);
    });
    return list;
  }

  /// 동아리 검색 — 키워드가 없으면 APPROVED 전체 목록, 있으면 이름/slug/회장명 검색
  @Override
  public List<ResponseClubList> searchClubs(String query, int page) {
    List<Club> clubs = (query == null || query.isBlank())
        ? clubRepository.findByStatus(ClubStatus.APPROVED)
        : clubRepository.searchByKeyword(query);
    List<ResponseClubList> list = new ArrayList<>();
    clubs.forEach(c -> {
      var resp = new ResponseClubList(c.getId(), c.getName(), c.getSlug(), getPresidentName(c), c.getStatus(), getMemberCount(c), c.getCreatedAt());
      list.add(resp);
    });
    return list;
  }

  /// 동아리 정보 수정 — 회장만 가능
  @Override
  public ResponseClubDetail updateClub(String userNumber, String slug, RequestClubUpdate req) {
    Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("동아리를 찾을 수 없습니다."));
    User user = userRepository.findByNumber(userNumber).orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다."));
    if (club.getPresident() == null || !club.getPresident().getId().equals(user.getId())) {
      throw new IllegalArgumentException("회장만 동아리 정보를 수정할 수 있습니다.");
    }

    club.setName(req.name());
    club.setDescription(req.description());
    club.setAutoApprove(req.autoApprove());

    clubRepository.save(club);
    return new ResponseClubDetail(club.getId(), club.getName(), club.getSlug(), club.getDescription(), getPresidentName(club), club.getAutoApprove(), getMemberCount(club), club.getCreatedAt(), club.getStatus());
  }

  /// 동아리 상태 변경 — 관리자 전용 (승인/거절)
  @Override
  public ResponseClubDetail updateClubStatus(String slug, ClubStatus status, String reason, String adminNumber) {
    Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("동아리를 찾을 수 없습니다."));
    User admin = userRepository.findByNumber(adminNumber).orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));
    if (!admin.getRole().equals(UserRole.ROLE_ADMIN)) throw new IllegalArgumentException("유효하지 않은 요청입니다.");

    club.setStatus(status);
    club.setRejectReason(reason);
    clubRepository.save(club);

    // 승인 시 회장을 ClubMember(ROLE_PRESIDENT, APPROVED)로 자동 등록
    if (status == ClubStatus.APPROVED && club.getPresident() != null) {
      if (!clubMemberRepository.existsByUserAndClub(club.getPresident(), club)) {
        ClubMember president = new ClubMember();
        president.setClub(club);
        president.setUser(club.getPresident());
        president.setRole(ClubRole.ROLE_PRESIDENT);
        president.setStatus(ClubMemberStatus.APPROVED);
        president.setJoinedAt(LocalDateTime.now());
        clubMemberRepository.save(president);
      }
    }

    return new ResponseClubDetail(club.getId(), club.getName(), club.getSlug(), club.getDescription(), getPresidentName(club), club.getAutoApprove(), getMemberCount(club), club.getCreatedAt(), club.getStatus());
  }

  /// 동아리 부원 수 조회
  private int getMemberCount(Club club) {
    return clubMemberRepository.countByClubId(club.getId());
  }

  /// 회장 이름 조회 — null-safe (회장이 없는 경우 null 반환)
  private String getPresidentName(Club club) {
    return club.getPresident() != null ? club.getPresident().getName() : null;
  }
}
