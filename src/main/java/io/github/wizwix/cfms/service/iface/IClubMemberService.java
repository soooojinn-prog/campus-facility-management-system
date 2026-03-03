package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.response.club.ResponseClubMember;
import io.github.wizwix.cfms.model.enums.ClubRole;

import java.util.List;

public interface IClubMemberService {
  List<ResponseClubMember> getMembers(String slug);

  ResponseClubMember joinClub(String name, String slug);

  void kickMember(String requesterNumber, String slug, String targetNumber);

  ResponseClubMember setRole(String requesterNumber, String slug, String targetNumber, ClubRole clubRole);
}
