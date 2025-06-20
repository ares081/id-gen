package com.ares.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import com.ares.config.properties.RateLimiterProperties;
import com.ares.factory.ratelimit.CustomerRateLimiter;
import com.ares.factory.ratelimit.GuavaRateLimiter;
import com.ares.factory.ratelimit.RateLimit;
import com.ares.factory.ratelimit.RateLimiterType;
import com.ares.factory.ratelimit.RedisRateLimiter;
import com.ares.factory.ratelimit.Resilience4jRateLimiter;
import com.ares.factory.ratelimit.SlidingWindowRateLimiter;

@Component
public class RateLimiterFactory {
  private final Map<String, CustomerRateLimiter> rateLimiterCache = new ConcurrentHashMap<>();

  private final RateLimiterProperties properties;
  private final StringRedisTemplate stringRedisTemplate;

  public RateLimiterFactory(RateLimiterProperties properties,
      StringRedisTemplate stringRedisTemplate) {
    this.properties = properties;
    this.stringRedisTemplate = stringRedisTemplate;
  }

  /**
   * 根据限流注解创建相应的限流器
   */

  public CustomerRateLimiter createRateLimiter(RateLimit rateLimit, String fallbackKey) {
    String key = rateLimit.key().isEmpty() ? fallbackKey : rateLimit.key();

    return rateLimiterCache.computeIfAbsent(key, v -> {
      RateLimiterType type = rateLimit.type();

      // 如果配置了该key的特定配置，则应用配置
      if (properties.getApis().containsKey(key)) {

        RateLimiterProperties.ApiRateLimit apiConfig = properties.getApis().get(key);

        if (apiConfig.getType() != null) {
          type = RateLimiterType.valueOf(apiConfig.getType());
        }

        int permits = apiConfig.getPermits() > 0 ? apiConfig.getPermits() : rateLimit.permits();
        int timeWindow = apiConfig.getTimeWindowSeconds() > 0 ? apiConfig.getTimeWindowSeconds()
            : (int) rateLimit.timeUnit().toSeconds(rateLimit.timeWindow());
        return switch (type) {
          case REDIS -> createRedisRateLimiter(permits, timeWindow);
          case RESILIENCE4J -> createResilience4jRateLimiter(permits, timeWindow);
          case SLIDING_WINDOW -> createSlidingWindowRateLimiter(permits, timeWindow);
          default -> createGuavaRateLimiter(permits, timeWindow);
        };
      }

      // 否则使用注解上的配置
      return switch (type) {
        case REDIS -> createRedisRateLimiter(rateLimit);
        case RESILIENCE4J -> createResilience4jRateLimiter(rateLimit);
        case SLIDING_WINDOW -> createSlidingWindowRateLimiter(rateLimit);
        default -> createGuavaRateLimiter(rateLimit);
      };
    });
  }


  /**
   * 从配置中创建限流器
   */

  public CustomerRateLimiter createRateLimiterFromProperties(String key) {

    RateLimiterType type = RateLimiterType.valueOf(properties.getDefaultType());
    int permits = properties.getDefaultPermits();
    int timeWindow = properties.getDefaultTimeWindowSeconds();

    if (properties.getApis().containsKey(key)) {
      RateLimiterProperties.ApiRateLimit apiConfig = properties.getApis().get(key);
      if (apiConfig.getType() != null) {
        type = RateLimiterType.valueOf(apiConfig.getType());
      }
      if (apiConfig.getPermits() > 0) {
        permits = apiConfig.getPermits();
      }
      if (apiConfig.getTimeWindowSeconds() > 0) {
        timeWindow = apiConfig.getTimeWindowSeconds();
      }
    }
    return switch (type) {
      case REDIS -> createRedisRateLimiter(permits, timeWindow);
      case RESILIENCE4J -> createResilience4jRateLimiter(permits, timeWindow);
      case SLIDING_WINDOW -> createSlidingWindowRateLimiter(permits, timeWindow);
      default -> createGuavaRateLimiter(permits, timeWindow);
    };
  }

  private CustomerRateLimiter createGuavaRateLimiter(RateLimit rateLimit) {
    double permitsPerSecond = calculatePermitsPerSecond(rateLimit);
    return new GuavaRateLimiter(permitsPerSecond);
  }

  private CustomerRateLimiter createGuavaRateLimiter(int permits, int timeWindowSeconds) {
    double permitsPerSecond = (double) permits / timeWindowSeconds;
    return new GuavaRateLimiter(permitsPerSecond);
  }

  private CustomerRateLimiter createRedisRateLimiter(RateLimit rateLimit) {
    if (stringRedisTemplate == null) {
      throw new IllegalStateException("Redis template is not available for Redis rate limiter");
    }
    int windowSeconds = (int) rateLimit.timeUnit().toSeconds(rateLimit.timeWindow());
    return new RedisRateLimiter(stringRedisTemplate, rateLimit.permits(), windowSeconds);
  }

  private CustomerRateLimiter createRedisRateLimiter(int permits, int timeWindowSeconds) {
    if (stringRedisTemplate == null) {
      throw new IllegalStateException("Redis template is not available for Redis rate limiter");
    }
    return new RedisRateLimiter(stringRedisTemplate, permits, timeWindowSeconds);
  }

  private CustomerRateLimiter createResilience4jRateLimiter(RateLimit rateLimit) {
    return new Resilience4jRateLimiter(
        rateLimit.permits(),
        rateLimit.timeWindow(),
        rateLimit.timeUnit());
  }

  private CustomerRateLimiter createResilience4jRateLimiter(int permits, int timeWindowSeconds) {
    return new Resilience4jRateLimiter(
        permits,
        timeWindowSeconds,
        TimeUnit.SECONDS);
  }

  private CustomerRateLimiter createSlidingWindowRateLimiter(RateLimit rateLimit) {
    return new SlidingWindowRateLimiter(
        rateLimit.permits(),
        rateLimit.timeWindow(),
        rateLimit.timeUnit());
  }

  private CustomerRateLimiter createSlidingWindowRateLimiter(int permits, int timeWindowSeconds) {
    return new SlidingWindowRateLimiter(
        permits,
        timeWindowSeconds,
        TimeUnit.SECONDS);
  }

  private double calculatePermitsPerSecond(RateLimit rateLimit) {
    // 将配置的时间窗口和许可数转换为每秒许可数
    double timeWindowInSeconds = rateLimit.timeUnit().toSeconds(rateLimit.timeWindow());
    if (timeWindowInSeconds == 0)
      timeWindowInSeconds = 1; // 防止除零
    return (double) rateLimit.permits() / timeWindowInSeconds;
  }
}
