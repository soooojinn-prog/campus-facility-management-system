package io.github.wizwix.cfms.global.config;

import io.github.wizwix.cfms.global.error.JsonErrorResponseWriter;
import io.github.wizwix.cfms.global.ratelimit.PasswordResetRateLimitFilter;
import io.github.wizwix.cfms.global.security.JwtAuthenticationProcessingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final JsonErrorResponseWriter errorWriter;
  private final JwtAuthenticationProcessingFilter jwtAuthenticationFilter;
  private final PasswordResetRateLimitFilter rateLimitFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    AuthenticationEntryPoint entryPoint = (request, response, ex) -> errorWriter.write(response, HttpStatus.UNAUTHORIZED, "Authentication required");
    AccessDeniedHandler deniedHandler = (request, response, ex) -> errorWriter.write(response, HttpStatus.FORBIDDEN, "Access denied");

    http.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults()).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(auth -> auth
        // ── 공개 API (비로그인 접근 가능) ──
        .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/register", "/api/auth/password-reset/**").permitAll()
        // GET 요청 중 공개 조회:
        //   /api/reservations (쿼리 파라미터용, e.g. ?roomId=1&date=...) — 타임라인 UI
        //   ※ /api/reservations/** 가 아닌 정확히 /api/reservations만 permitAll
        //   ※ /api/reservations/me, /api/reservations/{id} 는 아래 authenticated에 매치됨
        //   매처 순서 중요: 이 줄이 아래 "/api/reservations/**".authenticated() 보다 먼저 선언되어야 함
        .requestMatchers(HttpMethod.GET, "/api/library/reading-rooms/reservations/me", "/api/library/study-rooms/reservations/me").authenticated()
        .requestMatchers(HttpMethod.GET, "/api/clubs/my").authenticated()
        .requestMatchers(HttpMethod.GET, "/api/dorms/applications/me").authenticated()
        .requestMatchers(HttpMethod.GET, "/api/buildings/**", "/api/rooms/**", "/api/cafeterias/**", "/api/dorms/**", "/api/clubs/**", "/api/counseling/counselors", "/api/counseling/slots", "/api/reservations", "/api/library/**").permitAll()
        // ── 관리자 전용 ──
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        // ── 로그인 필요 API ──
        .requestMatchers("/api/users/**", "/api/reservations/**").authenticated().requestMatchers(HttpMethod.POST, "/api/clubs/**").authenticated().requestMatchers(HttpMethod.PATCH, "/api/clubs/**").authenticated().requestMatchers(HttpMethod.DELETE, "/api/clubs/**").authenticated().requestMatchers("/api/**").authenticated()
        // ── 프론트엔드 (React SPA) 및 정적 리소스 ──
        .anyRequest().permitAll()).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class).addFilterBefore(rateLimitFilter, JwtAuthenticationProcessingFilter.class).exceptionHandling(conf -> conf.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler));
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
