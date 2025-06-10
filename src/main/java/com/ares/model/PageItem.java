package com.ares.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class PageItem {

  private int page;
  private int size;
  private long total;
  private long totalElements;
}
