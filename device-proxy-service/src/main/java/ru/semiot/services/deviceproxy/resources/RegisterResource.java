package ru.semiot.services.deviceproxy.resources;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.io.IOUtils;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.semiot.commons.namespaces.RDF;
import ru.semiot.semiot.commons.namespaces.SSNCOM;
import ru.semiot.services.deviceproxy.ServiceConfig;
import ru.semiot.services.deviceproxy.WAMPClient;

public class RegisterResource extends CoapResource {

    private static final ServiceConfig config
            = ConfigFactory.create(ServiceConfig.class);
    private static final Logger logger = LoggerFactory.getLogger(
            RegisterResource.class);
    private static final String WAMP = "WAMP";
    private final WAMPClient wampClient = WAMPClient.getInstance();

    public RegisterResource() {
        super("register");
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        Model description = toModel(exchange.getRequestText());

        if (!description.isEmpty()) {
            mapEndpoints(description);

            wampClient.publish(config.topicsRegister(), toString(description));

            exchange.respond(CoAP.ResponseCode.CREATED);
        } else {
            logger.warn("Received a request without payload or in wrong format");
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
        }
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        super.handleDELETE(exchange);
    }

    private Model toModel(final String payload) {
        return ModelFactory.createDefaultModel().read(
                IOUtils.toInputStream(payload),
                null,
                config.wampMessageFormat());
    }

    private void mapEndpoints(final Model source) {
        final Model tmp = ModelFactory.createDefaultModel();
        ResIterator sensors = source.listResourcesWithProperty(
                SSNCOM.hasCommunicationEndpoint);

        sensors.forEachRemaining((Resource sensor) -> {
            StmtIterator stmts = sensor.listProperties(
                    SSNCOM.hasCommunicationEndpoint);

            stmts.forEachRemaining((Statement stmt) -> {
                final String uri = stmt.getObject().asResource().getURI();
                final Resource wampEndpoint = ResourceFactory.createResource(
                        config.mappingToWAMP(uri));

                //Declare a new CommunicationEndpoint (WAMP)
                tmp.add(sensor, SSNCOM.hasCommunicationEndpoint, wampEndpoint)
                        .add(wampEndpoint, RDF.type, SSNCOM.CommunicationEndpoint)
                        .add(wampEndpoint, SSNCOM.protocol, WAMP);
            });
        });

        source.add(tmp);
    }

    private String toString(final Model model) {
        try (Writer writer = new StringWriter()) {
            model.write(writer, config.wampMessageFormat());
            return writer.toString();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

}
