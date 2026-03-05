package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.response.club.ResponseClubMember;
import io.github.wizwix.cfms.dto.response.club.ResponseMyClub;
import io.github.wizwix.cfms.exception.NotFoundException;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.club.Club;
import io.github.wizwix.cfms.model.club.ClubMember;
import io.github.wizwix.cfms.model.enums.ClubMemberStatus;
import io.github.wizwix.cfms.model.enums.ClubRole;
import io.github.wizwix.cfms.repo.UserRepository;
import io.github.wizwix.cfms.repo.club.ClubMemberRepository;
import io.github.wizwix.cfms.repo.club.ClubRepository;
import io.github.wizwix.cfms.service.iface.IClubMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/// 동아리 부원 관리 서비스
///
/// 부원 목록 조회, 가입 신청, 추방, 역할 변경 기능을 제공한다.
/// 추방/역할 변경은 해당 동아리의 회장만 수행할 수 있다.
@Service
@RequiredArgsConstructor
@Transactional
public class ClubMemberService implements IClubMemberService {
  private final ClubMemberRepository clubMemberRepository;
  private final ClubRepository clubRepository;
  private final UserRepository userRepository;

  /// 내가 가입한 동아리 목록 — 마이페이지 동아리 탭
  @Override
  public List<ResponseMyClub> getMyClubs(String userNumber) {
    User user = userRepository.findByNumber(userNumber).orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다."));
    return clubMemberRepository.findByUserAndStatusIn(user, List.of(ClubMemberStatus.APPROVED, ClubMemberStatus.PENDING)).stream().map(m -> {
      var club = m.getClub();
      String presidentName = club.getPresident() != null ? club.getPresident().getName() : null;
      return new ResponseMyClub(club.getId(), club.getName(), club.getSlug(), presidentName, club.getStatus(), m.getRole(), m.getStatus(), m.getJoinedAt());
    }).toList();
  }

  /// 동아리 부원 목록 조회 — status 미지정 시 APPROVED만 반환
  @Override
  public List<ResponseClubMember> getMembers(String slug, ClubMemberStatus status) {
    var queryStatus = status != null ? status : ClubMemberStatus.APPROVED;
    var list = new ArrayList<ResponseClubMember>();
    clubMemberRepository.findByClubSlugAndStatus(slug, queryStatus).forEach(m -> {
      var rcm = new ResponseClubMember(m.getUser().getId(), m.getUser().getName(), m.getUser().getNumber(), m.getRole(), m.getJoinedAt());
      list.add(rcm);
    });
    return list;
  }

  /// 동아리 가입 신청 — 회장 자기 가입 차단 + 중복 가입 차단
  @Override
  public ResponseClubMember joinClub(String userNumber, String slug) {
    User user = userRepository.findByNumber(userNumber).orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다."));
    Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("동아리를 찾을 수 없습니다."));
    if (club.getPresident() != null && club.getPresident().getId().equals(user.getId())) {
      throw new IllegalArgumentException("동아리 회장은 자신의 동아리에 가입 신청할 수 없습니다.");
    }
    if (clubMemberRepository.existsByUserAndClub(user, club)) {
      throw new IllegalArgumentException("이미 해당 동아리에 가입 신청한 이력이 있습니다.");
    }

    ClubMember member = new ClubMember();
    member.setClub(club);
    member.setUser(user);
    member.setJoinedAt(LocalDateTime.now());
    member.setRole(ClubRole.ROLE_MEMBER);
    member.setStatus(ClubMemberStatus.PENDING);
    clubMemberRepository.save(member);

    return new ResponseClubMember(member.getUser().getId(), member.getUser().getName(), member.getUser().getNumber(), member.getRole(), member.getJoinedAt());
  }

  /// 동아리 부원 추방 — 회장만 가능
  /// 해당 동아리의 회장인지 검증 후, 대상 부원의 상태를 LEFT로 변경
  @Override
  public void kickMember(String requesterNumber, String slug, Long targetId) {
    // 요청자가 해당 동아리 회장인지 확인
    Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("동아리를 찾을 수 없습니다."));
    User requester = userRepository.findByNumber(requesterNumber).orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));
    if (club.getPresident() == null || !club.getPresident().getId().equals(requester.getId())) {
      throw new IllegalArgumentException("회장만 부원을 추방할 수 있습니다.");
    }

    // 대상 부원을 해당 동아리에서 조회
    User target = userRepository.findById(targetId).orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다."));
    ClubMember member = clubMemberRepository.findByUserAndClubSlug(target, slug).orElseThrow(() -> new NotFoundException("해당 동아리의 부원이 아닙니다."));
    member.setStatus(ClubMemberStatus.LEFT);
    clubMemberRepository.save(member);
  }

  /// 동아리 부원 역할 변경 — 회장만 가능
  /// 해당 동아리의 회장인지 검증 후, 대상 부원의 역할을 변경
  @Override
  public ResponseClubMember setRole(String requesterNumber, String slug, Long targetId, ClubRole clubRole) {
    User requester = userRepository.findByNumber(requesterNumber).orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다."));
    Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("동아리를 찾을 수 없습니다."));
    if (club.getPresident() == null || !club.getPresident().getId().equals(requester.getId())) {
      throw new IllegalArgumentException("회장만 역할을 변경할 수 있습니다.");
    }

    // 대상 부원을 해당 동아리에서 조회
    User target = userRepository.findById(targetId).orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다."));
    ClubMember member = clubMemberRepository.findByUserAndClubSlug(target, slug).orElseThrow(() -> new NotFoundException("해당 동아리의 부원이 아닙니다."));
    member.setRole(clubRole);
    clubMemberRepository.save(member);

    return new ResponseClubMember(member.getUser().getId(), member.getUser().getName(), member.getUser().getNumber(), member.getRole(), member.getJoinedAt());
  }
}
