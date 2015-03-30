package ru.semiot.services.deviceproxy.handlers.wamp;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import ru.semiot.services.deviceproxy.handlers.coap.NewObservationHandler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import java.io.IOException;
import java.io.StringReader;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.RiotException;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.semiot.commons.namespaces.EMTR;
import ru.semiot.semiot.commons.namespaces.HMTR;
import ru.semiot.semiot.commons.namespaces.SSN;
import ru.semiot.services.deviceproxy.ServiceConfig;
import ru.semiot.services.deviceproxy.WAMPClient;
import rx.Observer;

public class NewDeviceHandler implements Observer<String> {

    private static final Logger logger = LoggerFactory.getLogger(NewDeviceHandler.class);
    private static final ServiceConfig config = ConfigFactory.create(
            ServiceConfig.class);
    private static final String queryFile = 
            "/ru/semiot/services/deviceproxy/handlers/wamp/NewDeviceHandler/query.sparql";
    private static final String VAR_COAP = "coap";
    private static final String VAR_WAMP = "wamp";
    private final WAMPClient wampClient = WAMPClient.getInstance();
    private final Model schema;
    private final Query query;

    public NewDeviceHandler() {
        this.schema = FileManager.get().loadModel(SSN.URI);
        this.schema.add(FileManager.get().loadModel(HMTR.URI));
        this.schema.add(FileManager.get().loadModel(EMTR.URI));

        try {
            this.query = QueryFactory.create(IOUtils.toString(
                    this.getClass().getResourceAsStream(queryFile)));
        } catch (IOException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public void onCompleted() {
        logger.debug("completed");
    }

    @Override
    public void onError(Throwable e) {
        logger.warn(e.getMessage(), e);
    }

    @Override
    public void onNext(final String message) {
        try {
            Model description = ModelFactory.createDefaultModel()
                    .read(IOUtils.toInputStream(message), null,
                            config.wampMessageFormat());
            if (!description.isEmpty()) {
                InfModel infModel = ModelFactory.createRDFSModel(
                        schema, description);

                try (QueryExecution qexec = QueryExecutionFactory.create(
                        query, infModel)) {
                    final ResultSet results = qexec.execSelect();
                    while (results.hasNext()) {
                        final QuerySolution soln = results.next();
                        
                        final Resource coap = soln.getResource(VAR_COAP);
                        final Resource wamp = soln.getResource(VAR_WAMP);

                        logger.debug("Mapping {} to {}", coap.getURI(), wamp.getURI());
                        
                        final NewObservationHandler handler = 
                                new NewObservationHandler(getWampTopic(wamp.getURI()));

                        final CoapClient coapClient = new CoapClient(coap.getURI());
                        final CoapObserveRelation rel = coapClient.observe(handler);
                        
                        //So the handler could cancel the subscription.
                        handler.setRelation(rel);
                    }
                    
                    wampClient.publish(config.topicsNewAndObserving(), message);
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
    
    private String getWampTopic(final String wampUri) {
        return wampUri.split("\\?topic=")[1];
    }

}
