package com.ares.controller;

import com.ares.common.Response;
import com.ares.service.snowflake.SnowflakeService;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/idgen")
public class IdGenController {

  @Resource
  private SnowflakeService snowflakeService;

  @RequestMapping
  public Response<Object> next() {
    Long id = snowflakeService.nextId();
    Map<String, Long> map = new HashMap<>();
    map.put("id", id);
    return new Response<>(0, map);
  }

  @PostMapping
  public Response<Object> nextPost(@RequestBody Map<String, Object> body) {
    Long id = snowflakeService.nextId();
    Map<String, Long> map = new HashMap<>();
    map.put("id", id);
    return new Response<>(0, map);
  }
}
