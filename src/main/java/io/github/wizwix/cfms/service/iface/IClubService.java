package io.github.wizwix.cfms.service.iface;

import io.github.wizwix.cfms.model.Club;
import io.github.wizwix.cfms.model.enums.ClubStatus;

import java.util.List;

public interface IClubService {
  List<Club> getClubsByStatus(ClubStatus status);
}
