package io.github.wizwix.cfms.repo.club;

import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.club.ClubMember;
import io.github.wizwix.cfms.model.enums.ClubMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
  List<ClubMember> findByClubSlug(String slug);

  List<ClubMember> findByClubSlugAndStatus(String slug, ClubMemberStatus status);

  Optional<ClubMember> findByUser(User user);

  int countByClubId(Long clubId);
}
