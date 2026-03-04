package io.github.wizwix.cfms.global.config.dev.base;

import io.github.wizwix.cfms.repo.ReservationRepository;
import io.github.wizwix.cfms.repo.counceling.CounselingReservationRepository;
import io.github.wizwix.cfms.repo.dorm.DormApplicationRepository;
import io.github.wizwix.cfms.repo.library.LibrarySeatReservationRepository;
import io.github.wizwix.cfms.repo.library.LibraryStudyRoomReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataOrchestrator {
  private final List<DevDataLoader> loaders;
  private final ReservationRepository reservationRepo;
  private final DormApplicationRepository dormAppRepo;
  private final CounselingReservationRepository counselingResRepo;
  private final LibrarySeatReservationRepository seatResRepo;
  private final LibraryStudyRoomReservationRepository studyRoomResRepo;

  /// 종료 시 dev 데이터 정리.
  /// - 역순 unload: load 순서(Building→Room→...)의 반대로 실행하여 FK 제약 위반 방지
  /// - 개별 try-catch: 한 로더가 실패해도 나머지 로더는 계속 실행
  @EventListener(ContextClosedEvent.class)
  public void onShutdown() {
    log.info("Dev Profile: Cleaning up sample data...");
    // 사용자가 실행 중 생성한 데이터를 먼저 삭제 — FK 제약 위반 방지
    log.info("Dev Profile: Cleaning up user-created data...");
    reservationRepo.deleteAll();
    dormAppRepo.deleteAll();
    counselingResRepo.deleteAll();
    seatResRepo.deleteAll();
    studyRoomResRepo.deleteAll();

    List<DevDataLoader> reversed = new ArrayList<>(loaders);
    Collections.reverse(reversed);
    reversed.forEach(loader -> {
      try {
        loader.unload();
      } catch (Exception e) {
        log.error("Failed to unload {}: {}", loader.getClass().getSimpleName(), e.getMessage());
      }
    });
  }

  @EventListener(ContextRefreshedEvent.class)
  public void onStartup() {
    log.info("Dev Profile: Loading sample data...");
    loaders.forEach(DevDataLoader::load);
  }
}
