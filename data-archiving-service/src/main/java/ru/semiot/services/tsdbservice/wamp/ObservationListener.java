package ru.semiot.services.tsdbservice.wamp;

import java.io.StringReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.DCTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.services.tsdbservice.model.Observation;
import ru.semiot.services.tsdbservice.TSDBClient;
import rx.Observer;

import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.QUDT;
import ru.semiot.commons.namespaces.SSN;

public class ObservationListener implements Observer<String> {

    private static final Logger logger = LoggerFactory
            .getLogger(ObservationListener.class);
    private static final String TIMESTAMP = "timestamp";
    private static final String VALUE = "value";
    private static final String PROPERTY = "property";
    private static final String FEATURE_OF_INTEREST = "feature_of_interest";
    private static final String SENSOR_URI = "sensor_uri";
    private final String system_id;

    private static final Query METRICS_QUERY = QueryFactory
            .create(NamespaceUtils.newSPARQLQuery(
                    "SELECT ?timestamp ?property ?value ?feature_of_interest ?sensor_uri"
                            + "{" + " ?x a ssn:Observation ;"
                            + "     ssn:observedProperty ?property ;"
                            + "     ssn:observationResultTime ?timestamp ;"
                            + "     ssn:observationResult ?result ;"
                            + "     ssn:observedBy ?sensor_uri ."
                            + " ?result ssn:hasValue ?res_value ."
                            + " ?res_value qudt:quantityValue ?value ."
                            + " OPTIONAL {?x ssn:featureOfInterest ?feature_of_interest}"
                            + "}",
                    SSN.class, QUDT.class, DCTerms.class));

    public ObservationListener(String system_id) {
        this.system_id = system_id;
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
        try {
            Model description = ModelFactory.createDefaultModel().read(
                    new StringReader(message), null,
                    RDFLanguages.TURTLE.getName());
            if (!description.isEmpty()) {
                QueryExecution qe = QueryExecutionFactory.create(METRICS_QUERY,
                        description);
                ResultSet metrics = qe.execSelect();
                while (metrics.hasNext()) {
                    QuerySolution qs = metrics.next();
                    String timestamp = qs.getLiteral(TIMESTAMP).getString();
                    String sensorUri = qs.getResource(SENSOR_URI).getURI();
                    Resource featureOfInterestResource = qs
                            .getResource(FEATURE_OF_INTEREST);
                    String value = qs.getLiteral(VALUE).getString();
                    String property = qs.getResource(PROPERTY).getURI();
                    if (StringUtils.isNotBlank(value)
                            && StringUtils.isNotBlank(property)
                            && StringUtils.isNotBlank(timestamp)
                            && StringUtils.isNoneBlank(sensorUri)) {
                        String featureOfInterest = null;
                        if (featureOfInterestResource != null) {
                            featureOfInterest = featureOfInterestResource
                                    .getURI();
                        }
                        TSDBClient.getInstance().write(
                                new Observation(sensorUri.substring(
                                        sensorUri.lastIndexOf("/") + 1),
                                        system_id, timestamp, property,
                                        featureOfInterest, value));
                    } else {
                        logger.warn(
                                "In the message not found required fields for the metric");
                    }
                }
            } else {
                logger.warn("Received an empty message or in a wrong format!");
            }
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
