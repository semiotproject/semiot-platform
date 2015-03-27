package ru.semiot.services.devicedirectory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.StringReader;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;

public class RegisterListener implements Observer<String> {

    private static final Logger logger = LoggerFactory.getLogger(
            RegisterListener.class);
    private static final String LANG = "TURTLE";
    private static final ServiceConfig config
            = ConfigFactory.create(ServiceConfig.class);
    private final RDFStore rdfStore = RDFStore.getInstance();
    private final WAMPClient wampClient = WAMPClient.getInstance();

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        logger.warn(e.getMessage(), e);
    }

    @Override
    public void onNext(String message) {
        try {
            Model description = ModelFactory.createDefaultModel()
                    .read(new StringReader(message), null, LANG);
            if (!description.isEmpty()) {
                //TODO: check that the registering sensor doesn't exist already. Do we need it?

                rdfStore.save(description);

                wampClient.publish(config.topicsNewDevice(), message);
            } else {
                logger.warn("Received an empty message or in a wrong format!");
            }
        } catch (RiotException ex) {
            logger.warn(ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
