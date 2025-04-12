package com.ares;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication
public class IdGenApplication {


  public static void main(String[] args) {
    SpringApplication.run(IdGenApplication.class, args);
  }
}
