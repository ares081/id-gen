package com.ares.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ares.domain.SnowflakeIdGeneratorEntity;
import com.ares.domain.SnowflakeIdRepository;
import com.ares.factory.SnowflakeIdGenerator;
import com.ares.factory.SnowflakeIdGeneratorFactory;
import com.github.benmanes.caffeine.cache.Cache;

@Configuration
public class SnowflakeGeneratorConfig {

  @Bean
  public SnowflakeIdGeneratorFactory snowflakeIdGeneratorFactory(
      Cache<String, SnowflakeIdGenerator> generatorCache,
      Cache<String, SnowflakeIdGeneratorEntity> stateCache,
      SnowflakeIdRepository snowflakeIdRepository) {
    return new SnowflakeIdGeneratorFactory(generatorCache, stateCache, snowflakeIdRepository);
  }
}
