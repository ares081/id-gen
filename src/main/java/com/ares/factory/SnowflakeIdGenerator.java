package com.ares.factory;

import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ares.domain.SnowflakeIdGeneratorEntity;
import lombok.Getter;

@Getter
@Component
public class SnowflakeIdGenerator {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final Long START_TIME = 1744300800000L;

  // 每部分占用的位数
  private final long workerIdBits = 5L; // 工作机器ID
  private final long dataCenterIdBits = 5L; // 数据中心ID
  private final long sequenceBits = 12L; // 序列号

  // 最大值
  private final long maxWorkerId = ~(-1L << workerIdBits);
  private final long maxDataCenterId = ~(-1L << dataCenterIdBits);
  private final long maxSequence = ~(-1L << sequenceBits);

  // 移位
  private final long workerIdShift = sequenceBits;
  private final long dataCenterIdShift = sequenceBits + workerIdBits;
  private final long timestampShift = sequenceBits + workerIdBits + dataCenterIdBits;

  public long workerId; // 工作机器ID
  public long dataCenterId; // 数据中心ID
  public long sequence = 0L; // 序列号
  public long lastTimestamp = -1L; // 上次生成ID的时间戳

  // 操作计数器
  private AtomicLong operationCounter = new AtomicLong(0);
  // 上次持久化的时间戳
  private long lastPersistentTimestamp = System.currentTimeMillis();
  // 配置项
  private final int PERSIST_THRESHOLD = 1000; // 每生成1000个ID持久化一次
  private final long PERSIST_TIME_INTERVAL = 60000; // 至少每60秒持久化一次

  // 是否需要持久化的标志
  private volatile boolean needPersist = false;

  private SnowflakeIdGeneratorEntity snowflakeIdEntity;

  public SnowflakeIdGenerator() {
    this(0L, 0L);
  }

  SnowflakeIdGenerator(long workerId, long dataCenterId) {
    // 检查参数合法性
    if (workerId > maxWorkerId || workerId < 0) {
      throw new IllegalArgumentException(
          "Worker ID can't be greater than " + maxWorkerId + " or less than 0");
    }
    if (dataCenterId > maxDataCenterId || dataCenterId < 0) {
      throw new IllegalArgumentException(
          "DataCenter ID can't be greater than " + maxDataCenterId + " or less than 0");
    }
    this.workerId = workerId;
    this.dataCenterId = dataCenterId;
  }

  // 业务ID生成状态初始化
  SnowflakeIdGenerator(SnowflakeIdGeneratorEntity snowflakeIdEntity) {
    this(snowflakeIdEntity.getWorkerId(), snowflakeIdEntity.getDataCenterId());
    this.snowflakeIdEntity = snowflakeIdEntity;
    this.lastTimestamp = snowflakeIdEntity.getLastTimestamp();
    this.sequence = snowflakeIdEntity.getSequence();
  }

  /**
   * 生成下一个ID
   */
  public synchronized long genNextId() {

    long timestamp = System.currentTimeMillis();

    // 检查时钟是否回拨
    if (timestamp < lastTimestamp) {
      throw new RuntimeException("Clock moved backwards. Refusing to generate id for "
          + (lastTimestamp - timestamp) + " milliseconds");
    }

    // 如果是同一时间生成的，则进行序列号递增
    if (lastTimestamp == timestamp) {
      sequence = (sequence + 1) & maxSequence;
      // 序列号已经用完，等待下一毫秒
      if (sequence == 0) {
        timestamp = waitNextMillis(lastTimestamp);
      }
    } else {
      // 不同毫秒内，序列号重置为0
      sequence = 0L;
    }
    lastTimestamp = timestamp;

    // 如果有业务ID生成状态，更新它
    if (snowflakeIdEntity != null) {
      snowflakeIdEntity.setLastTimestamp(lastTimestamp);
      snowflakeIdEntity.setSequence(sequence);

      // 校验是否需要持久化
      checkAndSetPersistFlag();
    }

    // 构造返回ID
    return ((timestamp - START_TIME) << timestampShift) |
        (dataCenterId << dataCenterIdShift) |
        (workerId << workerIdShift) |
        sequence;
  }

  /**
   * 等待下一个毫秒
   */
  private long waitNextMillis(long lastTimestamp) {
    long timestamp = System.currentTimeMillis();
    while (timestamp <= lastTimestamp) {
      timestamp = System.currentTimeMillis();
    }
    return timestamp;
  }

  /**
   * 获取业务ID生成状态
   */
  public SnowflakeIdGeneratorEntity getSnowflakeIdEntity() {
    return this.snowflakeIdEntity;
  }

  /**
   * 检查并设置持久化标志
   */
  private void checkAndSetPersistFlag() {
    long counter = operationCounter.incrementAndGet();
    long currentTime = System.currentTimeMillis();

    if (counter >= PERSIST_THRESHOLD ||
        (currentTime - lastPersistentTimestamp) >= PERSIST_TIME_INTERVAL) {
      needPersist = true;
    }
  }

  /**
   * 如有需要，进行持久化并重置计数器
   * 
   * @return 是否执行了持久化操作
   */
  public boolean persistIfNeeded() {
    if (needPersist && snowflakeIdEntity != null) {
      needPersist = false;
      operationCounter.set(0);
      lastPersistentTimestamp = System.currentTimeMillis();
      return true;
    }
    return false;
  }

  // 用于强制持久化的方法
  public void forcePersist() {
    needPersist = true;
  }

}
