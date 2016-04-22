package ru.semiot.services.tsdbservice.rest;

import rx.Subscriber;

import javax.ws.rs.container.AsyncResponse;

public class ResponseStringOrError extends ResponseOrError<String> {

  private ResponseStringOrError(AsyncResponse response) {
    super(response);
  }

  public static Subscriber<String> responseStringOrError(
      AsyncResponse response) {
    return new ResponseStringOrError(response);
  }

  @Override
  public void onNext(String str) {
    response.resume(str);
  }

}
