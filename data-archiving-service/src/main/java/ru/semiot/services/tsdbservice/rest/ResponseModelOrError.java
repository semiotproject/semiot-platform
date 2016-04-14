package ru.semiot.services.tsdbservice.rest;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFLanguages;
import rx.Subscriber;

import javax.ws.rs.container.AsyncResponse;
import java.io.StringWriter;

public class ResponseModelOrError extends ResponseOrError<Model> {

    private ResponseModelOrError(AsyncResponse response) {
        super(response);
    }

    public static Subscriber<Model> responseModelOrError(
            AsyncResponse response) {
        return new ResponseModelOrError(response);
    }

    @Override
    public void onNext(Model model) {
        StringWriter writer = new StringWriter();
        model.write(writer, RDFLanguages.strLangJSONLD);

        response.resume(writer.toString());
    }
}
