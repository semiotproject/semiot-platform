package ru.semiot.services.tsdbservice.rest;

import com.datastax.driver.core.exceptions.InvalidQueryException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.restapi.AsyncResponseHelper;
import rx.Subscriber;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import java.io.StringWriter;

public class ResponseModelOrError extends Subscriber<Model> {

    private static final Logger logger = LoggerFactory.getLogger(ResponseModelOrError.class);
    private final AsyncResponse response;

    private ResponseModelOrError(AsyncResponse response) {
        this.response = response;
    }

    public static Subscriber<Model> responseModelOrError(AsyncResponse response) {
        return new ResponseModelOrError(response);
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        logger.warn(e.getMessage(), e);
        if (e instanceof InvalidQueryException) {
            response.resume(Response.status(Response.Status.BAD_REQUEST).build());
        } else {
            response.resume(e);
        }
    }

    @Override
    public void onNext(Model model) {
        StringWriter writer = new StringWriter();
        model.write(writer, RDFLanguages.strLangJSONLD);

        response.resume(writer.toString());
    }
}
