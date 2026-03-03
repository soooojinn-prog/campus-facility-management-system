package io.github.wizwix.cfms.global.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {
  private final JwtUtils utils;

  @Override
  protected void doFilterInternal(HttpServletRequest req, @NonNull HttpServletResponse resp, @NonNull FilterChain filterChain) throws ServletException, IOException {
    String token = null;

    if (req.getCookies() != null) {
      for (var cookie : req.getCookies()) {
        if ("jwt_token".equals(cookie.getName())) {
          token = cookie.getValue();
          break;
        }
      }
    }

    if (token != null) {
      try {
        Claims claims = utils.getClaims(token);
        String username = claims.getSubject();
        @SuppressWarnings("unchecked") List<String> roles = claims.get("roles", List.class);
        if (roles != null && username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          List<SimpleGrantedAuthority> authorities = roles.stream().map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role).map(SimpleGrantedAuthority::new).toList();
          var authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      } catch (Exception e) {
        SecurityContextHolder.clearContext();
      }
    }
    filterChain.doFilter(req, resp);
  }
}
