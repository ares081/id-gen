package com.ares;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.ares.common.MdcHelper;


public class MDCHelperTest extends IdGenApplicationTests {

  Logger logger = LoggerFactory.getLogger(MDCHelperTest.class);
  ThreadPoolExecutor executor = new ThreadPoolExecutor(
      10,
      100,
      10L,
      TimeUnit.SECONDS,
      new LinkedBlockingQueue<>());

  @Test
  public void testRunnable() {
    MDC.put("requestId", "1111111111111");
    logger.info("test runnable");
    for (int i = 0; i < 10; i++) {
      executor.execute(MdcHelper.run(() -> {
        long start = System.currentTimeMillis();
        logger.info("runnable task start time is:{}", start);
      }));
    }
    executor.shutdown();
    MDC.remove("requestId");
  }

  @Test
  public void testCallable() throws ExecutionException, InterruptedException {
    MDC.put("requestId", "1111111111111");
    logger.info("test callable");
    List<Future<String>> futures = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      Future<String> future = executor.submit(MdcHelper.call(
          () -> {
            long start = System.currentTimeMillis();
            logger.info("callable task start time is:{}", start);
            return Thread.currentThread().getName();
          }));
      futures.add(future);
    }
    for (Future<String> future : futures) {
      logger.info("task thread name is:{}", future.get());
    }
    executor.shutdown();
    MDC.remove("requestId");
  }

  @Test
  public void testRunnableSubmit() {
    MDC.put("requestId", "1111111111111");
    logger.info("test runnable submit");
    for (int i = 0; i < 10; i++) {
      executor.submit(MdcHelper.run(() -> {
        long start = System.currentTimeMillis();
        logger.info("runnable submit task time is:{}", start);
      }));
    }
    executor.shutdown();
    MDC.remove("requestId");
  }

  @Test
  public void testSupplerAndConsumer() {
    MDC.put("requestId", "1111111111111");
    logger.info("test supplier task");
    for (int i = 0; i < 10; i++) {
      CompletableFuture.supplyAsync(MdcHelper.supplier(() -> {
        long start = System.currentTimeMillis();
        logger.info("supplier task start time is:{}", start);
        return Thread.currentThread().getName();
      }), executor).exceptionally(throwable -> {
        logger.error("线程[{}]发生了异常[{}], 继续执行其他线程", Thread.currentThread().getName(),
            throwable.getMessage());
        return null;
      }).thenAccept(MdcHelper.consumer(name -> logger.info("sub task thread name is:{}", name)));
    }
  }

}
