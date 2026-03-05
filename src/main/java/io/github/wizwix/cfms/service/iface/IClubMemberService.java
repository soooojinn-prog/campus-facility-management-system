package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.response.club.ResponseClubMember;
import io.github.wizwix.cfms.dto.response.club.ResponseMyClub;
import io.github.wizwix.cfms.model.enums.ClubMemberStatus;
import io.github.wizwix.cfms.model.enums.ClubRole;

import java.util.List;

public interface IClubMemberService {
  /// 내가 가입한 동아리 목록 — 마이페이지 동아리 탭
  List<ResponseMyClub> getMyClubs(String userNumber);

  List<ResponseClubMember> getMembers(String slug, ClubMemberStatus status);


  ResponseClubMember joinClub(String name, String slug);

  void kickMember(String requesterNumber, String slug, Long targetId);

  ResponseClubMember setRole(String requesterNumber, String slug, Long targetId, ClubRole clubRole);
}
