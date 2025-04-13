package com.ares.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ares.factory.ratelimit.RateLimit;
import com.ares.factory.ratelimit.RateLimiterType;
import com.ares.model.Response;
import com.ares.service.SnowflakeIdGeneratorService;



@RestController
@RequestMapping("/v1/idgen")
public class IdGenController {

  private final SnowflakeIdGeneratorService snowflakeIdGeneratorService;


  IdGenController(SnowflakeIdGeneratorService snowflakeIdGeneratorService) {
    this.snowflakeIdGeneratorService = snowflakeIdGeneratorService;
  }

  @GetMapping("/{bizType}")
  public Response<Object> generateIdForBizType(@PathVariable String bizType) {
    Long id = snowflakeIdGeneratorService.generateIdForBizType(bizType);
    Map<String, Long> map = new HashMap<>();
    map.put("id", id);
    return Response.ok(map);
  }

  @RateLimit(type = RateLimiterType.SLIDING_WINDOW)
  @GetMapping
  public Response<Object> generateId() {
    Long id = snowflakeIdGeneratorService.generateId();
    Map<String, Long> map = new HashMap<>();
    map.put("id", id);
    return Response.ok(map);
  }

  @GetMapping("/batch/{bizType}/{count}")
  public Response<Object> getMethodName(@PathVariable String bizType, @PathVariable Integer count) {
    if (count == null) {
      count = 3;
    }
    List<Long> ids = snowflakeIdGeneratorService.generateBatchIds(bizType, count);
    Map<String, List<Long>> map = new HashMap<>();
    map.put("ids", ids);
    return Response.ok(map);
  }

}
