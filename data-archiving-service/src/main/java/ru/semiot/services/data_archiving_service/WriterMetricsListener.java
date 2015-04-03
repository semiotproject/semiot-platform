package ru.semiot.services.data_archiving_service;

import java.io.StringReader;
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

public class WriterMetricsListener implements Observer<String> {
	private static final Logger logger = LoggerFactory
			.getLogger(WriterMetricsListener.class);

	private static final String TIMESTAMP = "t";
	private static final String NAME_METRIC = "ob";
	private static final String VALUE = "v";
	private static final String TYPE = "type";

	private static final Query METRICS_QUERY = QueryFactory
			.create("prefix hmtr: <http://purl.org/NET/ssnext/heatmeters#> prefix meter: <http://purl.org/NET/ssnext/meters/core#> prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> prefix xsd: <http://www.w3.org/2001/XMLSchema#> SELECT ?t ?ob ?v ?type WHERE { {?x a hmtr:TemperatureObservation} UNION{ ?x a hmtr:HeatObservation} ?x ssn:observationResultTime ?t; ssn:observedBy ?ob; ssn:observationResult ?result. ?result ssn:hasValue ?value. ?value meter:hasQuantityValue ?v. ?x a ?type.}");

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
					String nameMetric = qs.getResource(NAME_METRIC).getURI();
					// ДОБАВИТЬ ПРОВЕРКУ НА ТИПЫ STRING->LONG
					String timestamp = qs.getLiteral(TIMESTAMP).getString();
					String value = qs.getLiteral(VALUE).getString();
					String type = qs.getResource(TYPE).getURI();
					if (StringUtils.isNotBlank(nameMetric)
							&& StringUtils.isNotBlank(value)
							&& StringUtils.isNotBlank(type)
							&& StringUtils.isNotBlank(timestamp)) {
						HashMap<String, String> tags = new HashMap<String, String>();
						tags.put(TYPE, type);
						WriterOpenTsdb.getInstance().send(
								nameMetric,
								value,
								DatatypeConverter.parseDateTime(timestamp)
										.getTimeInMillis(), tags);
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
