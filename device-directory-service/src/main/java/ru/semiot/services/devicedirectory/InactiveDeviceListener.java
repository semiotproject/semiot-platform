package ru.semiot.services.devicedirectory;

import java.io.StringReader;

import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class InactiveDeviceListener implements Observer<String> {

	private static final Logger logger = LoggerFactory
			.getLogger(InactiveDeviceListener.class);
	private static final String LANG = "TURTLE";
	private final RDFStore rdfStore = RDFStore.getInstance();
	private final WAMPClient wampClient = WAMPClient.getInstance();

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
				// TODO: check that the registering sensor doesn't exist
				// already. Do
				// we need it?
				logger.info("Save " + message);
				rdfStore.save(description);

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
