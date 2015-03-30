package ru.semiot.services.deviceproxy.handlers.wamp;

import ru.semiot.services.deviceproxy.handlers.coap.NewObservationHandler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import java.io.StringReader;
import java.util.Set;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.riot.RiotException;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.WebLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.semiot.commons.namespaces.RDF;
import ru.semiot.services.deviceproxy.ServiceConfig;
import ru.semiot.services.deviceproxy.WAMPClient;
import rx.Observer;

public class NewDeviceHandler implements Observer<String> {

    private static final Logger logger = LoggerFactory.getLogger(NewDeviceHandler.class);
    private static final ServiceConfig config = ConfigFactory.create(
            ServiceConfig.class);
    private final WAMPClient wampClient = WAMPClient.getInstance();
    /**
     * Query finds a path with attributes <code>if=sensor</code> and
     * <code>obs</code>.
     */
    private static final String QUERY = "if=sensor&obs";

    @Override
    public void onCompleted() {
        logger.debug("completed");
    }

    @Override
    public void onError(Throwable e) {
        logger.warn(e.getMessage(), e);
    }

    @Override
    public void onNext(String message) {
        try {
            Model description = ModelFactory.createDefaultModel()
                    .read(new StringReader(message), null,
                            config.wampMessageFormat());
            if (!description.isEmpty()) {
                //TODO: Filter by a class name, e.g. ssn:Sensor.
                ResIterator iter = description.listResourcesWithProperty(RDF.type);
                if (iter.hasNext()) {
                    Resource resource = iter.nextResource();

                    CoapClient coapClient = new CoapClient(resource.getURI());
                    Set<WebLink> links = coapClient.discover(QUERY);

                    for (WebLink link : links) {
                        final String uri = resource.getURI() + link.getURI();
                        logger.debug("Subscribing to {}", uri);

                        final NewObservationHandler handler
                                = new NewObservationHandler(uri);

                        coapClient.setURI(uri);
                        CoapObserveRelation rel = coapClient.observe(handler);
                        //So the handler could cancel the subscription.
                        handler.setRelation(rel);
                    }
                } else {
                    logger.warn("Can't find a resource in\n{}", message);
                }
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
