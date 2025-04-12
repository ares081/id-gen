package com.ares.model;

import org.springframework.web.bind.annotation.RequestHeader;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Request<T> {

  private T body;
  private RequestHeader header;

}
