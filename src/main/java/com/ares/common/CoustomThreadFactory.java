package com.ares.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoustomThreadFactory implements ThreadFactory {

  private final Logger logger = LoggerFactory.getLogger(CoustomThreadFactory.class);
  private final ThreadGroup group;
  private final String prefix;
  private final AtomicInteger threadNumber = new AtomicInteger(1);
  private final AtomicInteger totalThreadsCreated = new AtomicInteger(0);

  public CoustomThreadFactory(String groupName, String prefix) {
    this.prefix = prefix;
    this.group = new ThreadGroup(groupName);
  }

  @Override
  public Thread newThread(Runnable runnable) {
    Thread thread = new Thread(group, runnable, prefix + "-" + threadNumber.getAndIncrement());
    if (thread.isDaemon()) {
      thread.setDaemon(false);
    }
    if (thread.getPriority() != Thread.NORM_PRIORITY) {
      thread.setPriority(Thread.NORM_PRIORITY);
    }

    // 设置未捕获异常处理器
    thread.setUncaughtExceptionHandler((t, e) -> {
      logger.error("Thread: {}, threw exception: {}", t.getName(), e.getCause());
    });

    // 记录创建的线程数量
    totalThreadsCreated.incrementAndGet();
    return thread;
  }


  public ThreadGroup getThreadGroup() {
    return group;
  }

  public int getTotalThreadsCreated() {
    return totalThreadsCreated.get();
  }
}
