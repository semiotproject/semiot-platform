package ru.semiot.services.tsdbservice.wamp;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import rx.Observer;

import java.io.StringReader;
import static ru.semiot.services.tsdbservice.ServiceConfig.CONFIG;

public abstract class RDFMessageObserver implements Observer<String> {

    @Override
    public void onCompleted() {
    }

    @Override
    public void onNext(String message) {
        try {
            Model model = ModelFactory.createDefaultModel().read(
                    new StringReader(message), null, CONFIG.wampMessageFormat());

            onNext(model);
        } catch (Throwable e) {
            onError(e);
        }
    }

    public abstract void onNext(Model model);

    protected ResultSet query(Model model, String query) {
        return query(model, QueryFactory.create(query));
    }

    protected ResultSet query(Model model, Query query) {
        return QueryExecutionFactory.create(query, model).execSelect();
    }
}
