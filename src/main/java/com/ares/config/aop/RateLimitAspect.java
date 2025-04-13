package com.ares.config.aop;

import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.ares.config.properties.RateLimiterProperties;
import com.ares.factory.RateLimiterFactory;
import com.ares.factory.ratelimit.CustomerRateLimiter;
import com.ares.factory.ratelimit.RateLimit;

@Aspect
public class RateLimitAspect {

  @Autowired
  private RateLimiterProperties properties;


  private final RateLimiterFactory rateLimiterFactory;

  public RateLimitAspect(RateLimiterFactory rateLimiterFactory) {
    this.rateLimiterFactory = rateLimiterFactory;
  }



  @Around("@annotation(com.ares.factory.ratelimit.RateLimit)")
  public Object rateLimit(ProceedingJoinPoint point) throws Throwable {

    if (!properties.isEnabled()) {
      return point.proceed();
    }

    MethodSignature signature = (MethodSignature) point.getSignature();
    Method method = signature.getMethod();

    RateLimit rateLimit = method.getAnnotation(RateLimit.class);

    String methodName = method.getDeclaringClass().getName() + "." + method.getName();
    CustomerRateLimiter limiter = rateLimiterFactory.createRateLimiter(rateLimit, methodName);

    try {
      if (limiter.tryAcquire(methodName)) {
        return point.proceed();
      } else {
        // 获取自定义消息或使用默认消息
        String message = rateLimit.message();
        if (message.isEmpty()) {
          message = properties.getDefaultMessage();
        }
        if (properties.getApis().containsKey(methodName)
            && properties.getApis().get(methodName).getMessage() != null) {
          message = properties.getApis().get(methodName).getMessage();
        }
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, message);
      }
    } finally {
      limiter.release(methodName); // 如果有需要释放的资源
    }
  }
}
