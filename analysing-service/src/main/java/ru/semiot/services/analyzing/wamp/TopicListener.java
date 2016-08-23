package ru.semiot.services.analyzing.wamp;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.StringReader;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.analyzing.cep.Engine;
import rx.Observer;

@ApplicationScoped
@Named
public class TopicListener implements Observer<String> {

    private static final Logger logger = LoggerFactory
            .getLogger(TopicListener.class);
    @Inject
    Engine engine;

    public TopicListener() {
        logger.info("Created Listener");
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        logger.warn(e.getMessage(), e);
    }

    @Override
    public void onNext(String message) {
        Model description = ModelFactory.createDefaultModel().read(
                new StringReader(message), null, RDFLanguages.strLangJSONLD);
        if (!description.isEmpty()) {
            engine.appendData(message);
        }
    }

}
