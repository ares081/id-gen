package com.ares.common;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Response<T> {

  private int code;
  private String msg;
  private T data;

  public Response(int code, T data) {
    this.code = code;
    this.data = data;
  }

  public Response(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public Response(int code, String msg, T data) {
    this.code = code;
    this.msg = msg;
    this.data = data;
  }
}
