package com.ares.common.utils;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.MDC;

public class MdcHelper {

  public static <T> Callable<T> wrap(final Callable<T> callable,
      final Map<String, String> context) {
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


  public static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
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

  public static <T> Collection<? extends Callable<T>> decorate(
      final Collection<? extends Callable<T>> tasks) {
    if (tasks == null) {
      throw new NullPointerException();
    }
    return tasks.stream().map(task -> wrap(task, MDC.getCopyOfContextMap())).toList();
  }
}
