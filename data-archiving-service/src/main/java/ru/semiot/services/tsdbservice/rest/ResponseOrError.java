package ru.semiot.services.tsdbservice.rest;

import java.io.StringWriter;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.exceptions.InvalidQueryException;

import rx.Subscriber;

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
            response.resume(
                    Response.status(Response.Status.BAD_REQUEST).build());
        }
        response.resume(e);
    }
}
