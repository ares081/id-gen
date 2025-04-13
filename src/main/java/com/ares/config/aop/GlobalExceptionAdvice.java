package com.ares.config.aop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ares.model.Response;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionAdvice {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @ExceptionHandler(value = Exception.class)
  @ResponseBody
  public Response<Object> exceptionHandler(HttpServletRequest req, Exception e) {
    logger.error("Exception: error msg: {}", e.getMessage());
    return Response.failed(Response.FAILED_CODE, e.getMessage());
  }
}
