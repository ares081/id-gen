package com.ares.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestHeader {
  private String token;
  private String traceId;
}
