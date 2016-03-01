package ru.semiot.services.data_archiving_service;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.bind.DatatypeConverter;

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

import rx.Observer;

import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.QUDT;
import ru.semiot.commons.namespaces.SSN;

public class WriterMetricsListener implements Observer<String> {

    private static final Logger logger = LoggerFactory
            .getLogger(WriterMetricsListener.class);
    private static final String PERCENT = "%";
    private static final String SLASH = "/";
    private static final String TIMESTAMP = "timestamp";
    private static final String VALUE_TYPE = "value_type";
    private static final String VALUE = "value";
    private static final String PROPERTY = "property";
    private static final String ENUM_VALUE = "enum_value";
    private static final String FEATURE_OF_INTEREST = "feature_of_interest";
    private static final String SENSOR_URI = "sensor_uri";

    private static final Query METRICS_QUERY = QueryFactory.create(
            NamespaceUtils.newSPARQLQuery(
                    "SELECT ?timestamp ?property ?value_type ?value ?feature_of_interest ?sensor_uri"
                    + "{"
                    + " ?x a ssn:Observation ;"
                    + "     ssn:observedProperty ?property ;"
                    + "     ssn:observationResultTime ?timestamp ;"
                    + "     ssn:observationResult ?result ;"
                    + "     ssn:observedBy ?sensor_uri ."
                    + " ?result ssn:hasValue ?res_value ."
                    + " ?res_value a ?value_type ."
                    + "{"
                    + " ?res_value ssn:hasValue ?value"
                    + "} UNION {"
                    + " ?res_value qudt:quantityValue ?value"
                    + "}"
                    + "OPTIONAL {?x ssn:featureOfInterest ?feature_of_interest}"
                    + "}",
                    SSN.class, QUDT.class, DCTerms.class));

    private final String nameMetric; // временное решение

    public WriterMetricsListener(String nameMetric) {
        this.nameMetric = nameMetric;
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
                    new StringReader(message), null, RDFLanguages.TURTLE.getName());
            if (!description.isEmpty()) {
                QueryExecution qe = QueryExecutionFactory.create(METRICS_QUERY,
                        description);
                ResultSet metrics = qe.execSelect();
                while (metrics.hasNext()) {
                    QuerySolution qs = metrics.next();
                    String timestamp = qs.getLiteral(TIMESTAMP).getString();
                    String valueType = qs.getResource(VALUE_TYPE).getURI();
                    String sensorUri = qs.getResource(SENSOR_URI).getURI();
                    Resource featureOfInterest = qs.getResource(FEATURE_OF_INTEREST);
                    String value;
                    if (valueType.equals(QUDT.Enumeration.getURI())) {
                        value = qs.getResource(VALUE).getURI(); // instance enum uri 
                    } else {
                        value = qs.getLiteral(VALUE).getString(); // value simulator
                    }
                    String property = qs.getResource(PROPERTY).getURI();
                    if (StringUtils.isNotBlank(nameMetric)
                            && StringUtils.isNotBlank(value)
                            && StringUtils.isNotBlank(property)
                            && StringUtils.isNotBlank(timestamp)
                            && StringUtils.isNoneBlank(sensorUri)) {
                        HashMap<String, String> tags = new HashMap<>();
                        try {
                            Calendar calendar = DatatypeConverter
                                    .parseDateTime(timestamp);
                            tags.put(PROPERTY, encodeUrl(property));
                            tags.put(VALUE_TYPE, encodeUrl(valueType));
                            tags.put(SENSOR_URI, encodeUrl(sensorUri));
                            if (featureOfInterest != null) {
                                tags.put(FEATURE_OF_INTEREST,
                                        encodeUrl(featureOfInterest.getURI()));
                            }
                            if (valueType.equals(QUDT.Enumeration.getURI())) {
                                tags.put(ENUM_VALUE, encodeUrl(value));
                                WriterOpenTsdb.getInstance().send(
                                        nameMetric, 0,
                                        calendar.getTimeInMillis(), tags);
                            } else {
                                WriterOpenTsdb.getInstance().send(
                                        nameMetric, value,
                                        calendar.getTimeInMillis(), tags);
                            }
                        } catch (IllegalArgumentException e) {
                            logger.warn("Can't convert {} to calendar (xsd:dateTime)",
                                    timestamp);
                        }
                    } else {
                        logger.warn("In the message not found required fields for the metric");
                    }
                }

            } else {
                logger.warn("Received an empty message or in a wrong format!");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.name())
                    .replace(PERCENT, SLASH);
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
