package io.github.wizwix.cfms.global.ratelimit;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRateLimiter {
  private static final int MAX_ATTEMPTS = 5;
  private static final long WINDOW_MS = 60_000; // 1 minutes
  private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

  public boolean isAllowed(String key) {
    long now = System.currentTimeMillis();
    attempts.compute(key, (k, attempt) -> {
      if (attempt == null || now - attempt.startTime > WINDOW_MS) {return new Attempt(1, now);}
      if (attempt.count >= MAX_ATTEMPTS) {return attempt;}
      attempt.count++;
      return attempt;
    });
    return attempts.get(key).count <= MAX_ATTEMPTS;
  }

  @AllArgsConstructor
  private static class Attempt {
    int count;
    long startTime;
  }
}
