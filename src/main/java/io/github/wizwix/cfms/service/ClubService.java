package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.request.club.RequestClubCreate;
import io.github.wizwix.cfms.dto.request.club.RequestClubUpdate;
import io.github.wizwix.cfms.dto.response.club.ResponseClubDetail;
import io.github.wizwix.cfms.dto.response.club.ResponseClubList;
import io.github.wizwix.cfms.exception.NotFoundException;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.club.Club;
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

@Service
@RequiredArgsConstructor
@Transactional
public class ClubService implements IClubService {
  private final ClubMemberRepository clubMemberRepository;
  private final ClubRepository clubRepository;
  private final UserRepository userRepository;

  @Override
  public ResponseClubDetail createClub(String userNumber, RequestClubCreate req) {
    Club club = new Club();
    club.setName(req.name());
    club.setSlug(req.slug());
    club.setDescription(req.description());
    club.setAutoApprove(req.autoApprove());
    club.setStatus(ClubStatus.PENDING);
    club.setCreatedAt(LocalDateTime.now());
    clubRepository.save(club);
    return new ResponseClubDetail(club.getId(), club.getName(), club.getSlug(), club.getDescription(), club.getPresident().getName(), club.getAutoApprove(), getMemberCount(club), club.getCreatedAt(), club.getStatus());
  }

  @Override
  public ResponseClubDetail getClubDetail(String slug) {
    Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("동아리를 찾을 수 없습니다."));
    return new ResponseClubDetail(club.getId(), club.getName(), club.getSlug(), club.getDescription(), club.getPresident().getName(), club.getAutoApprove(), getMemberCount(club), club.getCreatedAt(), club.getStatus());
  }

  @Override
  public List<ResponseClubList> getClubListByStatus(ClubStatus status) {
    var list = new ArrayList<ResponseClubList>();
    clubRepository.findByStatus(status).forEach(c -> {
      var resp = new ResponseClubList(c.getId(), c.getName(), c.getSlug(), c.getPresident().getName(), c.getStatus(), getMemberCount(c), c.getCreatedAt());
      list.add(resp);
    });
    return list;
  }

  @Override
  public List<ResponseClubList> searchClubs(String query, int page) {
    List<Club> clubs = clubRepository.searchByKeyword(query);
    List<ResponseClubList> list = new ArrayList<>();
    clubs.forEach(c -> {
      var resp = new ResponseClubList(c.getId(), c.getName(), c.getSlug(), c.getPresident().getName(), c.getStatus(), getMemberCount(c), c.getCreatedAt());
      list.add(resp);
    });
    return list;
  }

  @Override
  public ResponseClubDetail updateClub(String userNumber, String slug, RequestClubUpdate req) {
    Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("동아리를 찾을 수 없습니다."));
    User user = userRepository.findByNumber(userNumber).orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다."));
    if (!club.getPresident().equals(user)) throw new IllegalArgumentException("유효하지 않은 요청입니다.");

    club.setName(req.name());
    club.setDescription(req.description());
    club.setAutoApprove(req.autoApprove());

    clubRepository.save(club);
    return new ResponseClubDetail(club.getId(), club.getName(), club.getSlug(), club.getDescription(), club.getPresident().getName(), club.getAutoApprove(), getMemberCount(club), club.getCreatedAt(), club.getStatus());
  }

  @Override
  public ResponseClubDetail updateClubStatus(String slug, ClubStatus status, String reason, String adminNumber) {
    Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("동아리를 찾을 수 없습니다."));
    User admin = userRepository.findByNumber(adminNumber).orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));
    if (!admin.getRole().equals(UserRole.ROLE_ADMIN)) throw new IllegalArgumentException("유효하지 않은 요청입니다.");

    club.setStatus(status);
    club.setRejectReason(reason);
    clubRepository.save(club);

    return new ResponseClubDetail(club.getId(), club.getName(), club.getSlug(), club.getDescription(), club.getPresident().getName(), club.getAutoApprove(), getMemberCount(club), club.getCreatedAt(), club.getStatus());
  }

  private int getMemberCount(Club club) {
    return clubMemberRepository.getClubMemberCountByClubId(club.getId());
  }
}
