package com.ares.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ares.domain.SnowflakeIdRecordEntity;
import com.ares.factory.SnowflakeIdGenerator;
import com.ares.factory.SnowflakeIdGeneratorFactory;
import jakarta.annotation.PreDestroy;

@Service
public class SnowflakeIdGeneratorService {

  // 默认生成器
  private final SnowflakeIdGenerator defaultGenerator;
  private final SnowflakeIdGeneratorFactory factory;
  private final SnowflakeIdRecordService snowflakeIdRecordService;


  public SnowflakeIdGeneratorService(SnowflakeIdGenerator defaultGenerator,
      SnowflakeIdGeneratorFactory factory, SnowflakeIdRecordService snowflakeIdRecordService) {
    this.factory = factory;
    this.defaultGenerator = defaultGenerator;
    this.snowflakeIdRecordService = snowflakeIdRecordService;
  }

  /**
   * 生成通用ID（不持久化）
   */
  public long generateId() {
    return defaultGenerator.genNextId();
  }


  /**
   * 按业务生成ID，同时持久化生成状态
   */
  public long generateIdForBizType(String bizType) {
    SnowflakeIdGenerator generator = factory.getOrCreateForBiz(bizType);

    // 生成ID
    long id = generator.genNextId();
    // 处理持久化状态，放入写缓冲
    factory.saveGeneratorStateIfNeeded(generator);

    // 创建并保存ID使用记录
    SnowflakeIdRecordEntity record = new SnowflakeIdRecordEntity();
    record.setGenId(id);
    record.setBizType(bizType);
    record.setWorkerId(generator.getWorkerId());
    record.setDataCenterId(generator.getDataCenterId());
    record.setSequence(generator.getSequence());
    record.setLastTimestamp(generator.getLastTimestamp());
    snowflakeIdRecordService.queueForSave(record);
    return id;
  }

  /**
   * 批量生成ID
   */

  public List<Long> generateBatchIds(String bizType, int count) {
    if (count <= 0) {
      throw new IllegalArgumentException("Count must be greater than 0");
    }
    List<Long> ids = new ArrayList<>(count);
    SnowflakeIdGenerator generator = factory.getOrCreateForBiz(bizType);
    List<SnowflakeIdRecordEntity> records = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      long id = generator.genNextId();
      ids.add(id);
      // 创建并保存ID使用记录
      SnowflakeIdRecordEntity record = new SnowflakeIdRecordEntity();
      record.setGenId(id);
      record.setBizType(bizType);
      record.setWorkerId(generator.getWorkerId());
      record.setDataCenterId(generator.getDataCenterId());
      record.setSequence(generator.getSequence());
      record.setLastTimestamp(generator.getLastTimestamp());
      records.add(record);
    }
    // 只处理一次持久化，提高批量性能
    factory.saveGeneratorStateIfNeeded(generator);
    snowflakeIdRecordService.queueForSave(records);
    return ids;
  }

  /**
   * 清除缓存中指定业务类型的生成器
   */
  public void removeBizCache(String bizType) {
    factory.removeFromCache(bizType);
  }


  /**
   * 清除所有缓存的生成器
   */
  public void clearAllCache() {
    factory.clearCache();
  }

  /**
   * 强制刷新所有持久化状态
   */
  public void forceFlushAll() {
    factory.saveAllGeneratorStates();
    snowflakeIdRecordService.saveBatch();
  }

  /**
   * 应用关闭前确保所有状态已持久化
   */
  @PreDestroy
  public void onApplicationShutdown() {
    forceFlushAll();
  }

}
