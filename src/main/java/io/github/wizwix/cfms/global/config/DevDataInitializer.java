package io.github.wizwix.cfms.global.config;

import io.github.wizwix.cfms.model.User;
import io.github.wizwix.cfms.model.enums.UserRole;
import io.github.wizwix.cfms.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DevDataInitializer {
  private final PasswordEncoder passwordEncoder;
  private final String devAdminEmail = "admin1@university.ac.kr";
  private final String devAdminNumber = "26999999";
  private final String devAdminPassword = "123";
  private final String devProfessorEmail = "professor1@university.ac.kr";
  private final String devProfessorNumber = "26100200";
  private final String devProfessorPassword = "123";
  private final String devStudentEmail = "student1@university.ac.kr";
  private final String devStudentNumber = "26001002";
  private final String devStudentPassword = "123";

  @Bean
  @Profile("dev")
  public CommandLineRunner createDevData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      if (userRepository.findByNumber(devStudentNumber).isEmpty()) {
        User std1 = new User();
        std1.setNumber(devStudentNumber);
        std1.setPassword(passwordEncoder.encode(devStudentPassword));
        std1.setEmail(devStudentEmail);
        std1.setRole(UserRole.ROLE_STUDENT);
        userRepository.save(std1);
        log.info("Dev Profile: Student account created (ID: {} / PW: {}})", devStudentNumber, devStudentPassword);
      }
    };
  }

  @Bean
  @Profile("!dev")
  public CommandLineRunner removeDevData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      List<String> devUserNames = List.of(devStudentNumber, devProfessorNumber, devAdminNumber);
      for (var devUserName : devUserNames) {
        userRepository.findByNumber(devUserName).ifPresent(user -> {
          boolean isDevEmail = switch (user.getRole()) {
            case ROLE_STUDENT -> devStudentEmail.equals(user.getEmail());
            case ROLE_PROFESSOR -> devProfessorEmail.equals(user.getEmail());
            case ROLE_ADMIN -> devAdminEmail.equals(user.getEmail());
          };
          boolean isDevPassword = switch (user.getRole()) {
            case ROLE_STUDENT -> passwordEncoder.matches(devStudentPassword, user.getPassword());
            case ROLE_PROFESSOR -> passwordEncoder.matches(devProfessorPassword, user.getPassword());
            case ROLE_ADMIN -> passwordEncoder.matches(devAdminPassword, user.getPassword());
          };
          if (isDevEmail && isDevPassword) userRepository.delete(user);
        });
      }
    };
  }
}
