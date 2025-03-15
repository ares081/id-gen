package com.ares.common.utils;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.slf4j.MDC;

public class MdcHelper {

  public static <T> Callable<T> call(final Callable<T> callable) {
    Map<String, String> context = MDC.getCopyOfContextMap();
    return () -> {
      Map<String, String> previous = MDC.getCopyOfContextMap();
      if (context == null || context.isEmpty()) {
        MDC.clear();
      } else {
        MDC.setContextMap(context);
      }
      try {
        return callable.call();
      } finally {
        if (previous == null) {

          MDC.clear();
        } else {
          MDC.setContextMap(previous);
        }
      }
    };
  }


  public static Runnable run(final Runnable runnable) {
    Map<String, String> context = MDC.getCopyOfContextMap();
    return () -> {
      Map<String, String> previous = MDC.getCopyOfContextMap();
      if (context == null || context.isEmpty()) {
        MDC.clear();
      } else {
        MDC.setContextMap(context);
      }
      try {
        runnable.run();
      } finally {
        if (previous == null) {
          MDC.clear();
        } else {
          MDC.setContextMap(previous);
        }
      }
    };
  }

  public static <T> Supplier<T> supplier(final Supplier<T> supplier) {
    Map<String, String> context = MDC.getCopyOfContextMap();
    return () -> {
      Map<String, String> previous = MDC.getCopyOfContextMap();
      if (context == null || context.isEmpty()) {
        MDC.clear();
      } else {
        MDC.setContextMap(context);
      }
      try {
        return supplier.get();
      } finally {
        if (previous == null) {
          MDC.clear();
        } else {
          MDC.setContextMap(previous);
        }
      }
    };
  }
}
