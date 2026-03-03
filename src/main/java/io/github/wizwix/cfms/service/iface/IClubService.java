package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.dto.request.club.RequestClubCreate;
import io.github.wizwix.cfms.dto.request.club.RequestClubUpdate;
import io.github.wizwix.cfms.dto.response.club.ResponseClubDetail;
import io.github.wizwix.cfms.dto.response.club.ResponseClubList;
import io.github.wizwix.cfms.model.enums.ClubStatus;

import java.util.List;

public interface IClubService {
  ResponseClubDetail createClub(String userNumber, RequestClubCreate req);

  ResponseClubDetail getClubDetail(String slug);

  List<ResponseClubList> getClubListByStatus(ClubStatus status);

  List<ResponseClubList> searchClubs(String query, int page);

  ResponseClubDetail updateClub(String userNumber, String slug, RequestClubUpdate req);

  ResponseClubDetail updateClubStatus(String slug, ClubStatus status, String reason, String adminNumber);
}
