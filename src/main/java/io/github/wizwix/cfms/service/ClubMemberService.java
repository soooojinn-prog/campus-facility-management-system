package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.dto.response.club.ResponseClubMember;
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

@Service
@RequiredArgsConstructor
@Transactional
public class ClubMemberService implements IClubMemberService {
  private final ClubMemberRepository clubMemberRepository;
  private final ClubRepository clubRepository;
  private final UserRepository userRepository;

  @Override
  public List<ResponseClubMember> getMembers(String slug) {
    var list = new ArrayList<ResponseClubMember>();
    clubMemberRepository.findByClubSlugAndStatus(slug, ClubMemberStatus.APPROVED).forEach(m -> {
      var rcm = new ResponseClubMember(m.getUser().getId(), m.getUser().getName(), m.getUser().getNumber(), m.getRole(), m.getJoinedAt());
      list.add(rcm);
    });
    return list;
  }

  @Override
  public ResponseClubMember joinClub(String userNumber, String slug) {
    User user = userRepository.findByNumber(userNumber).orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다."));
    Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("동아리를 찾을 수 없습니다."));

    ClubMember member = new ClubMember();
    member.setClub(club);
    member.setUser(user);
    member.setJoinedAt(LocalDateTime.now());
    member.setRole(ClubRole.ROLE_MEMBER);
    member.setStatus(ClubMemberStatus.PENDING);
    clubMemberRepository.save(member);

    return new ResponseClubMember(member.getId(), member.getUser().getName(), member.getUser().getNumber(), member.getRole(), member.getJoinedAt());
  }

  @Override
  public void kickMember(String requesterNumber, String slug, String targetNumber) {
    User user = userRepository.findByNumber(targetNumber).orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다."));
    ClubMember member = clubMemberRepository.findByUser(user).orElseThrow(() -> new NotFoundException("부원을 찾을 수 없습니다."));
    // 아예 해당 줄을 지우는 게 좋을까?
    member.setStatus(ClubMemberStatus.LEFT);
    clubMemberRepository.save(member);
  }

  @Override
  public ResponseClubMember setRole(String requesterNumber, String slug, String targetNumber, ClubRole clubRole) {
    User requester = userRepository.findByNumber(requesterNumber).orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다."));
    Club club = clubRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("동아리를 찾을 수 없습니다."));
    if (!club.getPresident().equals(requester)) throw new IllegalArgumentException("유효하지 않은 요청입니다.");

    User target = userRepository.findByNumber(targetNumber).orElseThrow(() -> new NotFoundException("학생을 찾을 수 없습니다."));
    ClubMember member = clubMemberRepository.findByUser(target).orElseThrow(() -> new NotFoundException("부원을 찾을 수 없습니다."));
    member.setRole(clubRole);
    clubMemberRepository.save(member);

    return new ResponseClubMember(member.getId(), member.getUser().getName(), member.getUser().getNumber(), member.getRole(), member.getJoinedAt());
  }
}
