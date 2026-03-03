package io.github.wizwix.cfms.global.config.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.github.wizwix.cfms.global.config.dev.base.BaseDevLoader;
import io.github.wizwix.cfms.global.config.dev.base.EntityReferenceDeserializer;
import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.club.Club;
import io.github.wizwix.cfms.repo.UserRepository;
import io.github.wizwix.cfms.repo.club.ClubRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

@Component
@Profile("dev")
@Slf4j
public class ClubDevLoader extends BaseDevLoader<Club> {
  private final ClubRepository clubRepo;
  private final UserRepository userRepo;

  public ClubDevLoader(ResourceLoader loader, ObjectMapper mapper, ClubRepository repo, UserRepository userRepo) {
    super(loader, mapper, Club.class, "data/dev/clubs.jsonc");
    this.clubRepo = repo;
    this.userRepo = userRepo;
  }

  @Override
  protected void configureMapper(ObjectMapper mapper) {
    SimpleModule module = new SimpleModule();
    // String -> User 변환
    module.addDeserializer(User.class, new EntityReferenceDeserializer<>(president -> userRepo.findByNumber(president).orElseThrow(() -> new RuntimeException("User[" + president + "] not found"))));
    // (추가로 필요한 부분이 있을 경우, 'addDeserializer' 사용
    mapper.registerModule(module);
  }

  @Override
  public void load() {
    // 모든 항목을 출력하는 대신, 저장한 동아리의 수를 한 줄로 출력하기
    // 모든 JSONC 파일 맨 위에 항목의 총 갯수를 적어둘 것
    LongAdder adder = new LongAdder();
    processItems(club -> {
      if (!clubRepo.existsBySlug(club.getSlug())) {
        clubRepo.save(club);
        adder.increment();
      }
    });
    log.info("Dev Profile: Loaded {} clubs", adder.sum());
  }

  @Override
  public void unload() {
    LongAdder adder = new LongAdder();
    processItems(club -> clubRepo.findBySlug(club.getSlug()).ifPresent(existing -> {
      clubRepo.delete(existing);
      adder.increment();
    }));
    log.info("Dev Profile: Unloaded {} clubs", adder.sum());
  }
}
