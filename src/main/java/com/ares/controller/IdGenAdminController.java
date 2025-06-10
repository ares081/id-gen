package com.ares.controller;

import com.ares.model.Response;
import com.ares.service.IdGeneratorAdminService;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin")
public class IdGenAdminController {

  @Resource
  private IdGeneratorAdminService idGeneratorAdminService;

  @GetMapping
  public Response<?> get(
      @PageableDefault(size = 20, page = 1, sort = "id", direction = Direction.DESC) Pageable pageable) {
    return idGeneratorAdminService.get(pageable);
  }
}
