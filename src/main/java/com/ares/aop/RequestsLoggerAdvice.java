package com.ares.aop;

import com.ares.service.snowflake.SnowflakeService;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RequestsLoggerAdvice {

  private final Logger logger = LoggerFactory.getLogger(RequestsLoggerAdvice.class);
  private final Gson gson;
  private final String REQUEST_ID = "requestId";

  @Resource
  private SnowflakeService snowflakeService;

  public RequestsLoggerAdvice(Gson gson) {
    this.gson = gson;
  }


  @Pointcut("execution(* com.*.controller.*.*(..))")
  private void requestLogger() {
  }

  @Around("requestLogger()")
  public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (requestAttributes == null) {
      return joinPoint.proceed();
    }
    HttpServletRequest request = requestAttributes.getRequest();
    String reqId = request.getRequestId();
    if (Strings.isNullOrEmpty(reqId) || Objects.equals(reqId, "0")) {
      reqId = request.getHeader(REQUEST_ID);
    }
    if (Strings.isNullOrEmpty(reqId) || Objects.equals(reqId, "0")) {
      //todo 生成一个全局唯一的请求id
      reqId = Long.toString(snowflakeService.nextId());
    }
    MDC.put(REQUEST_ID, reqId);

    //通过请求获取url,ip
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

    //获取方法所在的类名
    String className = joinPoint.getTarget().getClass().getName();
    //获取方法名
    String methodName = joinPoint.getSignature().getName();
    //获取参数名
    String[] parameterNamesArgs = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterNames();
    //获取参数值
    Object[] args = joinPoint.getArgs();

    joinPoint.getSignature().getDeclaringTypeName();

    Map<String, Object> params = new HashMap<>();
    for (int i = 0; i < parameterNamesArgs.length; i++) {
      params.put(parameterNamesArgs[i], args[i]);
    }

    Object result = null;
    try {
      logger.info(
          "start time:{} | remote ip:{} | url:{} | class:{} | func :{} | params:{} | header:{}",
          start, remoteIp, url, className, methodName, gson.toJson(params), gson.toJson(header));
      result = joinPoint.proceed();
    } catch (Exception e) {
      logger.error("请求日志记录异常", e);
    } finally {
      long end = System.currentTimeMillis();
      long cost = end - start;
      logger.info("end time:{} | cost :{}ms | result:{}", end, cost, gson.toJson(result));
    }
    return result;
  }

  private String getIpAddr(HttpServletRequest request) {
    String ipAddress = request.getHeader("x-forwarded-for");
    if (Strings.isNullOrEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("Proxy-Client-IP");
    }
    if (Strings.isNullOrEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getHeader("WL-Proxy-Client-IP");
    }
    if (Strings.isNullOrEmpty(ipAddress) || "unknown".equalsIgnoreCase(ipAddress)) {
      ipAddress = request.getRemoteAddr();
    }
    // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
    if (!Strings.isNullOrEmpty(ipAddress) && ipAddress.length() > 15) {
      if (ipAddress.indexOf(",") > 0) {
        ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
      }
    }
    // 或者这样也行,对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
    return ipAddress;
  }

}
