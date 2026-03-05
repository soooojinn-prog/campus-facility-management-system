package io.github.wizwix.cfms.controller;

import io.github.wizwix.cfms.dto.request.club.RequestClubCreate;
import io.github.wizwix.cfms.dto.request.club.RequestClubMemberRole;
import io.github.wizwix.cfms.dto.request.club.RequestClubUpdate;
import io.github.wizwix.cfms.dto.response.club.ResponseClubDetail;
import io.github.wizwix.cfms.dto.response.club.ResponseClubList;
import io.github.wizwix.cfms.dto.response.club.ResponseClubMember;
import io.github.wizwix.cfms.dto.response.club.ResponseMyClub;
import io.github.wizwix.cfms.model.enums.ClubMemberStatus;
import io.github.wizwix.cfms.service.iface.IClubMemberService;
import io.github.wizwix.cfms.service.iface.IClubService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubApiController {
  private final IClubMemberService clubMemberService;
  private final IClubService clubService;

  // TODO: 동아리 신청 전, 동일한 slug의 동아리가 있는지 확인하는 과정이 필요한가?

  /// 내가 가입한 동아리 목록 — 마이페이지용
  @GetMapping("/my")
  public ResponseEntity<List<ResponseMyClub>> myClubs(Authentication auth) {
    return ResponseEntity.ok(clubMemberService.getMyClubs(auth.getName()));
  }

  /// 동아리 개설 신청
  @PostMapping
  public ResponseEntity<ResponseClubDetail> create(@RequestBody @Valid RequestClubCreate req, Authentication auth) {
    return ResponseEntity.status(HttpStatus.CREATED).body(clubService.createClub(auth.getName(), req));
  }

  /// 동아리 상세 정보
  @GetMapping("/{slug}")
  public ResponseEntity<ResponseClubDetail> info(@PathVariable String slug) {
    return ResponseEntity.ok(clubService.getClubDetail(slug));
  }

  /// 동아리 가입 신청
  ///
  /// 가입 신청은 별다른 처리 없이 '가입' 버튼을 누르면 됨
  @PostMapping("/{slug}/members")
  public ResponseEntity<ResponseClubMember> join(@PathVariable String slug, Authentication auth) {
    return ResponseEntity.status(HttpStatus.CREATED).body(clubMemberService.joinClub(auth.getName(), slug));
  }

  /// 부원 탈퇴/추방 — userId는 DB primary key
  @DeleteMapping("/{slug}/members/{userId}")
  public ResponseEntity<Void> kick(@PathVariable String slug, @PathVariable Long userId, Authentication auth) {
    clubMemberService.kickMember(auth.getName(), slug, userId);
    return ResponseEntity.noContent().build();
  }

  /// 동아리 부원 목록 — status 파라미터로 PENDING 등 필터 가능 (기본: APPROVED)
  @GetMapping("/{slug}/members")
  public ResponseEntity<List<ResponseClubMember>> memberList(@PathVariable String slug, @RequestParam(required = false) ClubMemberStatus status) {
    return ResponseEntity.ok(clubMemberService.getMembers(slug, status));
  }

  /// 동아리 목록 검색
  @GetMapping
  public ResponseEntity<List<ResponseClubList>> search(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page) {
    return ResponseEntity.ok(clubService.searchClubs(q, page));
  }

  /// 부원 역할 변경 — userId는 DB primary key
  @PatchMapping("/{slug}/members/{userId}/role")
  public ResponseEntity<ResponseClubMember> setRole(@PathVariable String slug, @PathVariable Long userId, @RequestBody RequestClubMemberRole req, Authentication auth) {
    return ResponseEntity.ok(clubMemberService.setRole(auth.getName(), slug, userId, req.clubRole()));
  }

  /// 동아리 정보 수정
  @PatchMapping("/{slug}")
  public ResponseEntity<ResponseClubDetail> update(@PathVariable String slug, @RequestBody @Valid RequestClubUpdate req, Authentication auth) {
    return ResponseEntity.ok(clubService.updateClub(auth.getName(), slug, req));
  }
}
