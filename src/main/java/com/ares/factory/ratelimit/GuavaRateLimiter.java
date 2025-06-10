package com.ares.factory.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.common.util.concurrent.RateLimiter;

public class GuavaRateLimiter implements CustomerRateLimiter {


  private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
  private final double permitsPerSecond;

  public GuavaRateLimiter(double permitsPerSecond) {
    this.permitsPerSecond = permitsPerSecond;
  }

  @Override
  public boolean tryAcquire(String key) {
    RateLimiter rateLimiter =
        limiters.computeIfAbsent(key, k -> RateLimiter.create(permitsPerSecond));
    return rateLimiter.tryAcquire();
  }

}
