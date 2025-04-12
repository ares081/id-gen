package com.ares.config;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.ares.domain.SnowflakeIdGeneratorEntity;
import com.ares.factory.SnowflakeIdGenerator;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
@EnableScheduling
public class CaffeineCacheConfig {

  @Bean
  public Cache<String, SnowflakeIdGenerator> generatorCache() {
    return Caffeine.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .initialCapacity(100)
        .maximumSize(1000)
        .recordStats()
        .build();
  }

  @Bean
  public Cache<String, SnowflakeIdGeneratorEntity> stateCache() {
    return Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(15, TimeUnit.SECONDS)
        .recordStats()
        .build();
  }

  // @Bean
  public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager();
    cacheManager.setCaffeine(caffeine);
    cacheManager.setCacheNames(Arrays.asList("generatorCache", "stateCache"));
    return cacheManager;
  }


}
