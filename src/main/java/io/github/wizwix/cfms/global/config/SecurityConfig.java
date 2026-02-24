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

    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // allow public API
            .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/register", "/api/auth/password-reset/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/buildings/**", "/api/rooms/**", "/api/cafeteria/**", "/api/dorms/**", "/api/clubs/**").permitAll()
            // protect admin paths
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            // any other API call must be authenticated
            .requestMatchers("/api/users/**", "/api/reservations/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/api/clubs/**").authenticated()
            .requestMatchers(HttpMethod.PATCH, "/api/clubs/**").authenticated()
            .requestMatchers(HttpMethod.DELETE, "/api/clubs/**").authenticated()
            .requestMatchers("/api/**").authenticated()
            // everything else (React frontend, static files etc.) are permitted
            .anyRequest().permitAll()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(rateLimitFilter, JwtAuthenticationProcessingFilter.class)
        .exceptionHandling(conf -> conf.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler));
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
