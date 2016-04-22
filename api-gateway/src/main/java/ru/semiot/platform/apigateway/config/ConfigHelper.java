package ru.semiot.platform.apigateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import javax.servlet.AsyncContext;

public class ConfigHelper {

  private static final Logger logger = LoggerFactory.getLogger(ConfigHelper.class);

  public static <T> Subscriber<T> dispatch(AsyncContext ac, String path) {
    return new ConfigHelper.Dispatch<T>(ac, path);
  }

  private static class Dispatch<T> extends Subscriber<T> {

    private final AsyncContext ac;
    private final String path;

    public Dispatch(AsyncContext ac, String path) {
      this.ac = ac;
      this.path = path;
    }

    @Override
    public void onCompleted() {
      ac.dispatch(path);
    }

    @Override
    public void onError(Throwable e) {
      logger.warn(e.getMessage(), e);
    }

    @Override
    public void onNext(T t) {}

  }
}
