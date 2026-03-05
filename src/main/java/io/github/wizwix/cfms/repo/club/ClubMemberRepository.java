package io.github.wizwix.cfms.repo.club;

import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.club.Club;
import io.github.wizwix.cfms.model.club.ClubMember;
import io.github.wizwix.cfms.model.enums.ClubMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, Long> {
  /// 사용자가 가입한 동아리 목록 조회 (마이페이지용)
  List<ClubMember> findByUserAndStatusIn(User user, Collection<ClubMemberStatus> statuses);

  /// 동아리 중복 가입 방지용
  boolean existsByUserAndClub(User user, Club club);

  int countByClubId(Long clubId);

  List<ClubMember> findByClubSlug(String slug);

  List<ClubMember> findByClubSlugAndStatus(String slug, ClubMemberStatus status);

  Optional<ClubMember> findByUser(User user);

  /// 특정 동아리 + 특정 사용자로 부원 조회 (추방/역할 변경 시 사용)
  Optional<ClubMember> findByUserAndClubSlug(User user, String slug);
}
