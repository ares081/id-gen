package com.ares.config.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

@Configuration
@ConditionalOnClass(RateLimiterRegistry.class)
public class RateLimiterResilience4jConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public RateLimiterRegistry rateLimiterRegistry() {
    return RateLimiterRegistry.ofDefaults();
  }
}
