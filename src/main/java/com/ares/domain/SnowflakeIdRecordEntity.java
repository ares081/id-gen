package com.ares.domain;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "snd_snowflake_record")
public class SnowflakeIdRecordEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "gen_id")
  private Long genId;

  @Column(name = "biz_type")
  private String bizType;


  @Column(name = "sequence")
  private Long sequence;

  @Column(name = "worker_id")
  private Long workerId;

  @Column(name = "data_center_id")
  private Long dataCenterId;

  @Column(name = "last_timestamp")
  private Long lastTimestamp;

  @Column(name = "ctime", insertable = false, updatable = false)
  private LocalDateTime ctime;

  @Column(name = "mtime", insertable = false, updatable = false)
  private LocalDateTime mtime;

  public SnowflakeIdRecordEntity() {}

  public SnowflakeIdRecordEntity(Long genId, String bizType, Long workerId, Long dataCenterId) {
    this.genId = genId;
    this.bizType = bizType;
    this.lastTimestamp = -1L;
    this.sequence = 0L;
    this.workerId = workerId;
    this.dataCenterId = dataCenterId;
  }
}
