package com.ares.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.ares.domain.SnowflakeIdRecordEntity;
import com.ares.domain.SnowflakeIdRecordRepository;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;

@Service
public class SnowflakeIdRecordService {

  // 保存队列
  private final ConcurrentLinkedQueue<SnowflakeIdRecordEntity> saveQueue =
      new ConcurrentLinkedQueue<>();

  // 批量保存阈值
  private static final int BATCH_SIZE = 100;

  private final SnowflakeIdRecordRepository snowflakeIdRecordRepository;


  public SnowflakeIdRecordService(
      SnowflakeIdRecordRepository snowflakeIdRecordRepository) {
    this.snowflakeIdRecordRepository = snowflakeIdRecordRepository;
  }

  /**
   * 将ID记录添加到保存队列
   */
  public void queueForSave(SnowflakeIdRecordEntity record) {
    saveQueue.add(record);

    // 如果队列达到批量保存阈值，触发保存
    if (saveQueue.size() >= BATCH_SIZE) {
      triggerSave();
    }
  }

  /**
   * 将多个ID记录添加到保存队列
   */
  public void queueForSave(List<SnowflakeIdRecordEntity> records) {
    saveQueue.addAll(records);

    // 如果队列达到批量保存阈值，触发保存
    if (saveQueue.size() >= BATCH_SIZE) {
      triggerSave();
    }
  }

  /**
   * 触发保存操作
   */
  @Async
  public void triggerSave() {
    saveBatch();
  }

  /**
   * 批量保存队列中的记录
   */
  @Transactional
  public synchronized void saveBatch() {
    if (saveQueue.isEmpty()) {
      return;
    }

    List<SnowflakeIdRecordEntity> batch = new ArrayList<>(Math.min(saveQueue.size(), BATCH_SIZE));

    // 从队列中取出一批记录
    for (int i = 0; i < BATCH_SIZE && !saveQueue.isEmpty(); i++) {
      SnowflakeIdRecordEntity record = saveQueue.poll();
      if (record != null) {
        batch.add(record);
      }
    }
    // 批量保存
    if (!batch.isEmpty()) {
      snowflakeIdRecordRepository.saveAll(batch);
    }
  }

  /**
   * 定时保存队列中的记录（每5秒）
   */
  @Scheduled(fixedRate = 5000)
  public void scheduledSave() {
    if (!saveQueue.isEmpty()) {
      saveBatch();
    }
  }

  /**
   * 应用关闭前保存所有剩余记录
   */
  @PreDestroy
  public void saveAllBeforeShutdown() {
    while (!saveQueue.isEmpty()) {
      saveBatch();
    }
  }

  /**
   * 获取当前队列大小
   */
  public int getQueueSize() {
    return saveQueue.size();
  }

}
