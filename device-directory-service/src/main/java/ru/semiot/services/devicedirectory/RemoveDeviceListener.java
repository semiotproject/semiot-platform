package ru.semiot.services.devicedirectory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;

import com.hp.hpl.jena.query.ResultSet;

public class RemoveDeviceListener implements Observer<String> {

	private static final Logger logger = LoggerFactory
			.getLogger(RemoveDeviceListener.class);
	private static final String LANG = "TURTLE";
	private final RDFStore rdfStore = RDFStore.getInstance();
	private final WAMPClient wampClient = WAMPClient.getInstance();
	private static final String SEPARATOR = "\r\n";
	private static final String TOPICS_QUERY = new StringBuilder()
			.append("prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> ")
			.append("prefix ssncom: <http://purl.org/NET/ssnext/communication#> ")
			.append("SELECT ?q where{ ${URI_SYSTEM} ssn:hasSubsystem ?x. ?x a ssn:Sensor; ssncom:hasCommunicationEndpoint ?q. ")
			.append("?q ssncom:protocol \"WAMP\"}").toString();

	private static final String DELETE_SYSTEM = new StringBuilder()
			.append("PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#> ")
			.append("PREFIX ssncom: <http://purl.org/NET/ssnext/communication#> ")
			.append("DELETE {?comm ?ppp ?sss. ?sensor ?pp ?ss. URI_SYSTEM ?p ?s.} ")
			.append("WHERE{{URI_SYSTEM ssn:hasSubsystem ?sensor. ")
			.append("?sensor ssncom:hasCommunicationEndpoint ?comm. ?comm ?ppp ?sss. } ")
			.append("union {URI_SYSTEM ssn:hasSubsystem ?sensor. ?sensor ?pp ?ss.} ")
			.append("union {URI_SYSTEM ?p ?s.}}").toString();

	@Override
	public void onCompleted() {

	}

	@Override
	public void onError(Throwable e) {
		logger.warn(e.getMessage(), e);
	}

	@Override
	public void onNext(String message) {
		ResultSet rs = RDFStore.getInstance().select(
				TOPICS_QUERY.replace("${URI_SYSTEM}", message));
		StringBuffer topics = new StringBuffer();
		while (rs.hasNext()) {
			topics.append(rs.next().get("q").asResource().getURI());
			topics.append(SEPARATOR);
		}

		WAMPClient.getInstance().publish(
				Launcher.getConfig().topicsRemoveSensor(), topics.toString());

		String queryDelete = DELETE_SYSTEM.replaceAll("URI_SYSTEM", message); // написать
																				// паттерн
																				// для
																				// ${URI_SYSTEM}
		RDFStore.getInstance().update(queryDelete);

		logger.info("System {} was removed from rdfstore", message);
	}
}
