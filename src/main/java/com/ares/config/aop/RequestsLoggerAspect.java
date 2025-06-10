package com.ares.config.aop;

import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RequestsLoggerAspect {

  private final Logger logger = LoggerFactory.getLogger("api");
  private final Gson gson;
  private final String TRACE_ID = "traceId";

  public RequestsLoggerAspect(Gson gson) {
    this.gson = gson;
  }


  @Pointcut("execution(* com.*.controller.*.*(..))")
  private void requestLogger() {}

  @Around("requestLogger()")
  public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    ServletRequestAttributes requestAttributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (requestAttributes == null) {
      return joinPoint.proceed();
    }
    HttpServletRequest request = requestAttributes.getRequest();
    String reqId = request.getHeader(TRACE_ID);

    if (!StringUtils.hasText(reqId) || Objects.equals(reqId, "0")) {
      reqId = getTraceId();
    }
    MDC.put(TRACE_ID, reqId);

    // 通过请求获取url,ip
    String url = request.getRequestURL().toString();
    String remoteIp = getIpAddr(request);

    // 获取请求头信息
    Map<String, Object> header = new HashMap<>();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String key = headerNames.nextElement();
      String value = request.getHeader(key);
      header.put(key, value);
    }

    // 获取方法所在的类名
    String className = joinPoint.getTarget().getClass().getName();
    // 获取方法名
    String methodName = joinPoint.getSignature().getName();
    // 获取参数名
    String[] parameterNamesArgs =
        ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterNames();
    // 获取参数值
    Object[] args = joinPoint.getArgs();

    joinPoint.getSignature().getDeclaringTypeName();

    Map<String, Object> params = new HashMap<>();
    for (int i = 0; i < parameterNamesArgs.length; i++) {
      params.put(parameterNamesArgs[i], args[i]);
    }

    Object result = null;
    try {
      logger.info(
          "start:{} | remote ip:{} | url:{} | class:{} | func :{} | params:{} | header:{}",
          start, remoteIp, url, className, methodName, gson.toJson(params), gson.toJson(header));
      result = joinPoint.proceed();
    } catch (Exception e) {
      throw e;
    } finally {
      long end = System.currentTimeMillis();
      long cost = end - start;
      logger.info("end:{} | cost :{}ms | result:{}", end, cost, gson.toJson(result));
      MDC.remove(TRACE_ID);
    }
    return result;
  }

  private String getIpAddr(HttpServletRequest request) {
    String ipAddress = request.getHeader("x-forwarded-for");
    if (!StringUtils.hasText(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("Proxy-Client-IP");
    }
    if (!StringUtils.hasText(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("WL-Proxy-Client-IP");
    }
    if (!StringUtils.hasText(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }
    // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
    if (!StringUtils.hasText(ipAddress) && ipAddress.length() > 15) {
      if (ipAddress.indexOf(",") > 0) {
        ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
      }
    }
    // 或者这样也行,对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
    return ipAddress;
  }

  private String getTraceId() {
    UUID uuid = UUID.randomUUID();
    long l = uuid.getMostSignificantBits() >>> 26; // 取高24位
    String id = String.valueOf(l);

    if (id.length() > 6) {
      id = id.substring(0, 6);
    } else if (id.length() < 6) {
      id = String.format("%0" + 6 + "d", Long.parseLong(id));
    }
    return Instant.now().toEpochMilli() + id;
  }

}
