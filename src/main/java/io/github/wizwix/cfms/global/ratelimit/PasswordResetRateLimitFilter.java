package io.github.wizwix.cfms.global.ratelimit;

import io.github.wizwix.cfms.global.error.JsonErrorResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PasswordResetRateLimitFilter extends OncePerRequestFilter {
  private final JsonErrorResponseWriter errorWriter;
  private final InMemoryRateLimiter rateLimiter;

  @Override
  protected void doFilterInternal(HttpServletRequest req, @NonNull HttpServletResponse resp, @NonNull FilterChain chain) throws ServletException, IOException {
    if (req.getRequestURI().startsWith("/api/auth/password-reset")) {
      String key = req.getRemoteAddr();
      if (!rateLimiter.isAllowed(key)) {
        errorWriter.write(resp, HttpStatus.TOO_MANY_REQUESTS, "Too many requests");
        return;
      }
    }
    chain.doFilter(req, resp);
  }
}
