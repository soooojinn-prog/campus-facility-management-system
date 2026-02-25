package io.github.wizwix.cfms.service;

import io.github.wizwix.cfms.model.Club;
import io.github.wizwix.cfms.model.enums.ClubStatus;
import io.github.wizwix.cfms.repo.ClubRepository;
import io.github.wizwix.cfms.service.iface.IClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubService implements IClubService {
  private final ClubRepository clubRepository;

  @Override
  public List<Club> getClubsByStatus(ClubStatus status) {
    return clubRepository.findByStatus(status);
  }
}
