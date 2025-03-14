package com.ares;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class IdGenApplication {

  private final Logger logger = LoggerFactory.getLogger(IdGenApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(IdGenApplication.class, args);
  }

  @GetMapping("/health")
  public void healthCheck() throws Exception {
    logger.info("health check");
  }

}
