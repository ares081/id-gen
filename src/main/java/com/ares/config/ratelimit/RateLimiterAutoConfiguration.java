package com.ares.config.ratelimit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.ares.config.aop.RateLimitAspect;
import com.ares.config.properties.RateLimiterProperties;
import com.ares.factory.RateLimiterFactory;
import com.ares.factory.ratelimit.CustomerRateLimiter;

@AutoConfiguration
@ConditionalOnClass(CustomerRateLimiter.class)
@ConditionalOnProperty(prefix = "snd.rate-limiter", name = "enabled", havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(RateLimiterProperties.class)
@Import({RateLimiterRedisConfiguration.class, RateLimiterResilience4jConfiguration.class})
@ComponentScan("com.ares.factory.ratelimt")
public class RateLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RateLimiterFactory rateLimiterFactory(RateLimiterProperties properties,
            StringRedisTemplate redisTemplate) {
        return new RateLimiterFactory(properties, redisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitAspect rateLimitAspect(RateLimiterFactory rateLimiterFactory) {
        return new RateLimitAspect(rateLimiterFactory);
    }
}
