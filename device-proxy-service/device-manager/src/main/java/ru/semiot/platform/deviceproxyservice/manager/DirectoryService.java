package ru.semiot.platform.deviceproxyservice.manager;

import java.io.StringReader;
import java.net.URI;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RiotException;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.SSNCOM;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;

public class DirectoryService {

    private static final Logger logger = LoggerFactory
            .getLogger(DirectoryService.class);
    private static final Literal WAMP = ResourceFactory.createPlainLiteral("WAMP");
    private static final String GRAPH_PRIVATE = "urn:semiot:graphs:private";

    protected static final String QUERY_UPDATE_STATE_SYSTEM = "prefix saref: <http://ontology.tno.nl/saref#> "
            + "DELETE { <${URI_SYSTEM}> saref:hasState ?x } "
            + "INSERT { <${URI_SYSTEM}> saref:hasState <${STATE}> } "
            + "WHERE { <${URI_SYSTEM}> saref:hasState ?x }";
    private static final String GET_SYSTEM_URI = ""
            + "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#> \n"
            + "SELECT ?uri {?uri a ssn:System} LIMIT 1";

    private final RDFStore store;

    public DirectoryService(RDFStore store) {
        this.store = store;
    }

    public void inactiveDevice(String message) {
        String request = null;
        try {
            Model description = ModelFactory.createDefaultModel().read(
                    new StringReader(message), null, RDFLanguages.TURTLE.getName());

            if (!description.isEmpty()) {
                logger.info("Update " + message);

                QueryExecution qe = QueryExecutionFactory.create(
                        GET_SYSTEM_URI,
                        description);
                ResultSet systems = qe.execSelect();

                while (systems.hasNext()) {
                    QuerySolution qs = systems.next();
                    String uriSystem = qs.getResource("uri_system").getURI();
                    String state = qs.getResource("state").getURI();
                    request = QUERY_UPDATE_STATE_SYSTEM.replace("${URI_SYSTEM}", uriSystem)
                            .replace("${STATE}", state);
                    if (uriSystem != null && state != null) {
                        store.update(request);
                    }
                }
            } else {
                logger.warn("Received an empty message or in a wrong format!");
            }
        } catch (RiotException ex) {
            logger.warn(ex.getMessage(), ex);
        } catch (Exception ex) {
            logger.info("Exception with string: " + ((request != null && !request.isEmpty()) ? request : "request message is empty"));
            logger.error(ex.getMessage(), ex);
        }
    }

    public void addDevicePrototype(URI uri) {
        try {
            Model model = RDFDataMgr.loadModel(uri.toASCIIString(), RDFLanguages.TURTLE);

            store.save(model);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    /**
     * Doesn't check whether device already exists.
     *
     * @param deviceDescription
     * @return true if the given device successfully added.
     */
    public boolean addNewDevice(Device device, String deviceDescription) {
        try {
            Model description = ModelFactory.createDefaultModel().read(
                    new StringReader(deviceDescription), null, 
                            RDFLanguages.TURTLE.getName());

            if (!description.isEmpty()) {
                ResultSet qr = QueryExecutionFactory.create(
                        GET_SYSTEM_URI,
                        description).execSelect();

                if (qr.hasNext()) {
                    Resource system = qr.next().getResource(Vars.URI);

                    //Given uri is not a blank node
                    if (system.isURIResource()) {                       
                        store.save(description);
                        
                        //Add ssncom:hasCommunicationEndpoint to a private graph
                        Resource wampResource = ResourceFactory.createResource(system.getURI() + "/wamp");
                        Model privateDeviceInfo = ModelFactory
                                .createDefaultModel()
                                .add(system, SSNCOM.hasCommunicationEndpoint, wampResource)
                                .add(wampResource, RDF.type, SSNCOM.CommunicationEndpoint)
                                .add(wampResource, SSNCOM.topic, 
                                        ResourceFactory.createPlainLiteral(device.getId()))
                                .add(wampResource, SSNCOM.protocol, WAMP);

                        store.save(GRAPH_PRIVATE, privateDeviceInfo);
                        
                        return true;
                    } else {
                        logger.error("Device [{}] doesn't have URI!",
                                device.getId());
                    }
                } else {
                    logger.error("Can't find a system!");
                }
            } else {
                logger.warn("Received an empty message or in a wrong format!");
            }
        } catch (RiotException ex) {
            logger.warn(ex.getMessage(), ex);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        }

        return false;
    }

    private static abstract class Vars {

        public static final String URI = "uri";
    }
}
