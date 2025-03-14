package com.ares.service.snowflake;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SnowflakeService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());


  // 2025-03-04 11:48:34
  private static final long CUSTOM_EPOCH = 1740347596000L;

  private static final int TIMESTAMP_BITS = 41;
  // 用来表示数据中心ID所占用的位数。
  private static final int DC_ID_BITS = 2;
  // WORKER_ID_BITS: 用来表示worker ID所占用的位数。
  private static final int WORKER_ID_BITS = 8;
  // 序列号占用的位数
  private static final int SEQUENCE_BITS = 12;

  // private static final int MAX_DC_ID = -1L ^ (-1L << DC_ID_BITS);
  // private static final int MAX_DC_ID = (1 << DC_ID_BITS) - 1;
  private static final int MAX_DC_ID = ~(-1 << DC_ID_BITS);

  //private static final int MAX_WORKER_ID =-1L ^ (-1L <<  WORKER_ID_BITS) - ;
  //private static final int MAX_WORKER_ID = (1 << WORKER_ID_BITS) - 1;
  private static final int MAX_WORKER_ID = ~(-1 << WORKER_ID_BITS);

  // private static final int MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BITS) - 1;
  // private static final int MAX_SEQUENCE = (1 << SEQUENCE_BITS) - 1;
  // 生成序列的掩码，这里为4095, 上面注释结果相同
  private static final int MAX_SEQUENCE = ~(-1 << SEQUENCE_BITS);

  // 机器ID向左移12位
  private static final int WORKER_ID_SHIFT = SEQUENCE_BITS;
  // 数据中心ID向左移17位(12+5)
  private static final int DC_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
  // 时间截向左移22位(5+5+12)
  private static final int TIMESTAMP_SHIFT = DC_ID_BITS + WORKER_ID_BITS + SEQUENCE_BITS;

  // 上次生成ID的时间戳
  private long lastTimestamp = -1L;
  // 毫秒内序列 (0~4095)
  private long sequence = 0L;

  //todo 如下两个参数可进行配置

  // 工作机器ID (0~31)
  private final int workerId = 0;
  // 数据中心ID (0~31)
  private final int dcId = 0;


/*  public SnowflakeService(int dcId, int workerId) {
    if (dcId > MAX_DC_ID || dcId < 0) {
      String err = String.format("DC ID must be between 0 and %d", MAX_DC_ID);
      logger.error(err);
      throw new IllegalArgumentException(err);
    }
    if (workerId > MAX_WORKER_ID || workerId < 0) {
      String err = String.format("Worker ID must be between 0 and %d", MAX_WORKER_ID);
      logger.error(err);
      throw new IllegalArgumentException(err);
    }
    this.dcId = dcId;
    this.workerId = workerId;
  }*/

  public synchronized long nextId() {
    long currentTimestamp = getCurrentTimestamp();

    if (currentTimestamp < lastTimestamp) {
      String err = String.format(
          "Clock moved backwards! Refusing to generate ID for %d milliseconds",
          (lastTimestamp - currentTimestamp));
      logger.error(err);
      throw new IllegalStateException(err);
    }

    if (lastTimestamp == currentTimestamp) {
      sequence = (sequence + 1) & MAX_SEQUENCE;
      if (sequence == 0) {
        currentTimestamp = waitNextMillis(currentTimestamp);
      }
    } else {
      sequence = 0;
    }
    lastTimestamp = currentTimestamp;
    long id = ((currentTimestamp - CUSTOM_EPOCH) << TIMESTAMP_SHIFT)
        | ((long) dcId << DC_ID_SHIFT)
        | ((long) workerId << WORKER_ID_SHIFT)
        | sequence;
    logger.info("id generate is: {}", id);
    return id;
  }

  private long getCurrentTimestamp() {
    return System.currentTimeMillis();
  }

  private long waitNextMillis(long currentTimestamp) {
    while (currentTimestamp <= lastTimestamp) {
      currentTimestamp = getCurrentTimestamp();
    }
    return currentTimestamp;
  }
}
