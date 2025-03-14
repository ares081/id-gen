package com.ares.common;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Request<T> {

  private T req;
  private String requestId;

  public Request(T req) {
    this.req = req;
  }
}
