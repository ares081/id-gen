package com.ares.factory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ares.domain.SnowflakeIdGeneratorEntity;
import com.ares.domain.SnowflakeIdRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.gson.Gson;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;

@Component
public class SnowflakeIdGeneratorFactory {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private Gson gson;
  // Caffeine缓存实例
  private final Cache<String, SnowflakeIdGenerator> generatorCache;
  private final Cache<String, SnowflakeIdGeneratorEntity> stateCache;

  // 写缓冲区，存放待持久化的业务ID生成状态
  private final List<SnowflakeIdGeneratorEntity> dirtyStates = new ArrayList<>();

  // 默认配置
  private static final long DEFAULT_WORKER_ID = 0L;
  private static final long DEFAULT_DATACENTER_ID = 0L;

  private final SnowflakeIdRepository snowflakeIdRepository;

  public SnowflakeIdGeneratorFactory(Cache<String, SnowflakeIdGenerator> generatorCache,
      Cache<String, SnowflakeIdGeneratorEntity> stateCache,
      SnowflakeIdRepository snowflakeIdRepository) {
    this.snowflakeIdRepository = snowflakeIdRepository;
    this.generatorCache = generatorCache;
    this.stateCache = stateCache;
  }

  /**
   * 创建默认的雪花算法生成器
   */

  public SnowflakeIdGenerator createDefaultGenerator() {
    return new SnowflakeIdGenerator(DEFAULT_WORKER_ID, DEFAULT_DATACENTER_ID);
  }

  /**
   * 创建指定工作ID和数据中心ID的雪花算法生成器
   */
  public SnowflakeIdGenerator createDefaultGenerator(long workerId, long dataCenterId) {
    return new SnowflakeIdGenerator(workerId, dataCenterId);
  }

  /**
   * 获取或创建指定业务类型的ID生成器
   */
  @Transactional
  public SnowflakeIdGenerator getOrCreateForBiz(String bizType) {

    // 先从Caffeine缓存获取生成器
    SnowflakeIdGenerator generator = generatorCache.getIfPresent(bizType);
    if (generator != null) {
      return generator;
    }

    // 为业务类型生成唯一的工作ID和数据中心ID
    int hashCode = Math.abs(bizType.hashCode());
    long workerId = hashCode % 32; // 最大支持32个工作ID
    long dataCenterId = (hashCode / 32) % 32; // 最大支持32个数据中心ID

    // 缓存未命中，尝试从状态缓存获取
    SnowflakeIdGeneratorEntity state = stateCache.get(bizType, key -> {
      // 状态缓存未命中，从数据库加载
      Optional<SnowflakeIdGeneratorEntity> optionalState =
          snowflakeIdRepository.findByBizType(bizType, workerId, dataCenterId);
      if (optionalState.isPresent()) {
        return optionalState.get();
      } else {
        // 创建新的业务ID生成状态
        SnowflakeIdGeneratorEntity snowflakeIdEntity =
            new SnowflakeIdGeneratorEntity(bizType, workerId, dataCenterId);
        snowflakeIdRepository.save(snowflakeIdEntity);
        return snowflakeIdEntity;
      }
    });

    // 创建生成器并缓存
    generator = new SnowflakeIdGenerator(state);
    generatorCache.put(bizType, generator);
    return generator;
  }

  /**
   * 保存业务ID生成状态
   */
  public synchronized void saveGeneratorStateIfNeeded(SnowflakeIdGenerator generator) {
    if (!generator.persistIfNeeded()) {
      return;
    }

    SnowflakeIdGeneratorEntity entity = generator.getSnowflakeIdEntity();
    if (entity != null) {
      // 更新状态缓存
      stateCache.put(entity.getBizType(), entity);

      // 加入写缓冲区
      synchronized (dirtyStates) {
        // 如果已存在，先移除旧的
        dirtyStates.removeIf(s -> s.getBizType().equals(entity.getBizType()));
        dirtyStates.add(entity);
      }
      logger.info("save generator state: {}", gson.toJson(entity));
    }
  }

  /**
   * 批量持久化脏状态
   */
  public void flushDirtyStates() {
    List<SnowflakeIdGeneratorEntity> flushEntities;
    synchronized (dirtyStates) {
      if (dirtyStates.isEmpty()) {
        return;
      }
      // 复制一份脏状态列表以便在锁外处理
      flushEntities = new ArrayList<>(dirtyStates);
      dirtyStates.clear();
    }
    // 批量保存
    snowflakeIdRepository.saveAll(flushEntities);
  }

  /**
   * 定时刷新脏状态（每5秒执行一次）
   */
  @Scheduled(fixedRate = 5000)
  public void scheduledFlush() {
    flushDirtyStates();
  }

  /**
   * 强制保存所有生成器状态
   */
  public void saveAllGeneratorStates() {
    generatorCache.asMap().forEach((bizType, generator) -> {
      generator.forcePersist();
      saveGeneratorStateIfNeeded(generator);
    });
    // 立即刷新
    flushDirtyStates();
  }

  /**
   * 从缓存中移除生成器
   */
  public void removeFromCache(String bizType) {
    SnowflakeIdGenerator generator = generatorCache.getIfPresent(bizType);
    if (generator != null) {
      generator.forcePersist();
      SnowflakeIdGeneratorEntity entity = generator.getSnowflakeIdEntity();
      if (entity != null) {
        synchronized (dirtyStates) {
          dirtyStates.removeIf(s -> s.getBizType().equals(entity.getBizType()));
          dirtyStates.add(entity);
        }
      }
      // 从缓存中移除
      generatorCache.invalidate(bizType);
      stateCache.invalidate(bizType);
    }
    // 立即刷新
    flushDirtyStates();
  }

  /**
   * 清空缓存
   */
  @Transactional
  public void clearCache() {
    // 先保存所有状态
    saveAllGeneratorStates();

    // 再清空缓存
    generatorCache.invalidateAll();
    stateCache.invalidateAll();
  }

  /**
   * 获取缓存统计信息
   */
  public String getCacheStats() {
    return "Generator Cache: " + generatorCache.stats().toString() +
        "\nState Cache: " + stateCache.stats().toString();
  }

  /**
   * 应用关闭前刷新所有状态
   */
  @PreDestroy
  public void onShutdown() {
    saveAllGeneratorStates();
    logger.info("server is shutdown, timestamp: {}", Instant.now().toEpochMilli());
  }
}
