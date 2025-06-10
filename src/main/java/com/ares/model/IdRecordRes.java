package com.ares.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class IdRecordRes {
  private Long id;
  private Long genId;
  private String bizType;
  private Long sequence;
  private Long workerId;
  private Long dataCenterId;
  private Long lastTimestamp;
  private LocalDateTime ctime;
  private LocalDateTime mtime;
}
