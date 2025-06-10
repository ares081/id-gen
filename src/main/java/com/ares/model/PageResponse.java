package com.ares.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PageResponse<T> extends Response<T> {

  private PageItem page;

  public PageResponse(T data, PageItem pageItem) {
    super(OK_FLAG, OK_CODE, data);
    this.page = pageItem;
  }
}
