package com.ares.config.ratelimit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@ConditionalOnClass(StringRedisTemplate.class)
public class RateLimiterRedisConfiguration {

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(RedisConnectionFactory.class)
  public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }
}
