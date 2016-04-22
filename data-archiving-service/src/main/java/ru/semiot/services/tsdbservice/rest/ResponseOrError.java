package ru.semiot.services.tsdbservice.rest;

import com.datastax.driver.core.exceptions.InvalidQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

abstract class ResponseOrError<T> extends Subscriber<T> {


  private static final Logger logger = LoggerFactory.getLogger(ResponseOrError.class);
  protected final AsyncResponse response;

  public ResponseOrError(AsyncResponse response) {
    this.response = response;
  }

  @Override
  public void onCompleted() {

  }

  @Override
  public void onError(Throwable e) {
    logger.warn(e.getMessage(), e);
    if (e instanceof InvalidQueryException) {
      response.resume(Response.status(Response.Status.BAD_REQUEST).build());
    }
    response.resume(e);
  }
}
