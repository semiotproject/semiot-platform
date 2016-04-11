package ru.semiot.services.tsdbservice.rest;

import javax.ws.rs.container.AsyncResponse;

import rx.Subscriber;

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
