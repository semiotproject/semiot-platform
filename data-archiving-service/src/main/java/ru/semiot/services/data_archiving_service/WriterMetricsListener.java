package ru.semiot.services.data_archiving_service;

import java.io.StringReader;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

public class WriterMetricsListener implements Observer<String> {
	
	private static final Logger logger = LoggerFactory
			.getLogger(WriterMetricsListener.class);
	private static final String TIMESTAMP = "timestamp";
	private static final String VALUE_TYPE = "value_type";
	private static final String VALUE = "value";
	private static final String PROPERTY = "property";
	private static final String ENUM_VALUE = "enum_value";
	private static final String FEATURE_OF_INTEREST = "feature_of_interest";
	private static final String SOURCE = "source";
	
	private static final String QUDT_ENUMERATION = "http://www.qudt.org/qudt/owl/1.0.0/qudt/#Enumeration";
	private static final Query METRICS_QUERY = QueryFactory
			.create("prefix qudt: <http://www.qudt.org/qudt/owl/1.0.0/qudt/#> "
					+ "prefix dcterms: <http://purl.org/dc/terms/#> "
					+ "prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> "
					+ "SELECT ?timestamp ?property ?value_type ?value ?feature_of_interest ?identifier "
					+ "WHERE { ?x a ssn:Observation; "
					+ "ssn:observedProperty ?property; "
					+ "ssn:observationResultTime ?timestamp; "
					+ "ssn:observedBy ?sensor. "
					+ "?sensor dcterms:identifier ?source. "
					+ "ssn:observationResult ?result. "
					+ "?result ssn:hasValue ?res_value. "
					+ "?res_value a ?value_type. "
					+ "{?res_value ssn:hasValue  ?value} "
					+ "UNION {?res_value qudt:quantityValue ?value}"
					+ "OPTIONAL {?x ssn:featureOfInterest ?feature_of_interest}}");
	
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
					new StringReader(message), null, SubscribeListener.LANG);
			if (!description.isEmpty()) {
				QueryExecution qe = QueryExecutionFactory.create(METRICS_QUERY,
						description);
				ResultSet metrics = qe.execSelect();

				while (metrics.hasNext()) {
					QuerySolution qs = metrics.next();
					String timestamp = qs.getLiteral(TIMESTAMP).getString();
					String valueType = qs.getResource(VALUE_TYPE).getURI();
					String source = qs.getLiteral(SOURCE).getString();
					Resource featureOfInterest = qs.getResource(FEATURE_OF_INTEREST);
					String value;
					if (valueType.equals(QUDT_ENUMERATION)) {
						value = qs.getResource(VALUE).getURI(); // instance enum uri 
					} else {
						value =  qs.getLiteral(VALUE).getString(); // value simulator
					}
					String property = qs.getResource(PROPERTY).getURI();
					if (StringUtils.isNotBlank(nameMetric)
							&& StringUtils.isNotBlank(value)
							&& StringUtils.isNotBlank(property)
							&& StringUtils.isNotBlank(timestamp)
							&& StringUtils.isNoneBlank(source)) {
						HashMap<String, String> tags = new HashMap<String, String>();
						try {
							Calendar calendar = DatatypeConverter
									.parseDateTime(timestamp);
							tags.put(PROPERTY, property.replaceAll(":", "_").replace("#", "-"));// _ - 
							tags.put(VALUE_TYPE, valueType.replaceAll(":", "_").replace("#", "-"));
							tags.put(SOURCE, source);
							if(featureOfInterest != null) {
								tags.put(FEATURE_OF_INTEREST, featureOfInterest.getURI().replaceAll(":", "_").replace("#", "-"));
							}
							if(valueType.equals(QUDT_ENUMERATION)) { // для симуляторов
								tags.put(ENUM_VALUE, value.replaceAll(":", "_").replace("#", "-"));
								WriterOpenTsdb.getInstance().send(
										nameMetric, 0,
										calendar.getTimeInMillis(), tags);
							}
							else {
								WriterOpenTsdb.getInstance().send(
										nameMetric, value,
										calendar.getTimeInMillis(), tags);
							}
						} catch (IllegalArgumentException e) {
							logger.warn("Can't convert " + timestamp
									+ " to calendar (xsd:dateTime)");
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
}