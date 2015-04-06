package ru.semiot.services.data_archiving_service;

import java.io.StringReader;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class SubscribeListener implements Observer<String> {

	private static final Logger logger = LoggerFactory
			.getLogger(SubscribeListener.class);
	public static final String LANG = "TURTLE";
	private static final ServiceConfig config = ConfigFactory
			.create(ServiceConfig.class);

	private final WAMPClient wampClient = WAMPClient.getInstance();
	private static final String PREFIX_TOPIC = "topic=";

	private static final Query TOPICS_QUERY = QueryFactory
			.create(new StringBuilder()
					.append("prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> ")
					.append("prefix hmtr: <http://purl.org/NET/ssnext/heatmeters#> ")
					.append("prefix ssncom: <http://purl.org/NET/ssnext/communication#> ")
					.append("SELECT ?q where{ ?x a ssn:Sensor; ssncom:hasCommunicationEndpoint ?q. ")
					.append("?q ssncom:protocol \"WAMP\"}").toString());

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
					new StringReader(message), null, LANG);
			if (!description.isEmpty()) {
				QueryExecution qe = QueryExecutionFactory.create(TOPICS_QUERY,
						description);
				ResultSet topics = qe.execSelect();

				while (topics.hasNext()) {
					String topicName = parseTopicName(topics.next().toString());
					if (StringUtils.isNotBlank(topicName)) {
						wampClient.subscribe(topicName).subscribe(
								new WriterMetricsListener());
					} else {
						logger.warn("Name topic is blank");
					}
				}

			} else {
				logger.warn("Received an empty message or in a wrong format!");
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	private String parseTopicName(String uri) {
		int index = uri.indexOf(PREFIX_TOPIC);
		return index == -1 || index + PREFIX_TOPIC.length() + 3 > uri.length() ? null
				: uri.substring(index + PREFIX_TOPIC.length(), uri.length() - 1);
	}
}
