package ru.semiot.services.devicedirectory;

import java.io.StringReader;

import org.apache.jena.riot.RiotException;
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

public class InactiveDeviceListener implements Observer<String> {

	private static final Logger logger = LoggerFactory
			.getLogger(InactiveDeviceListener.class);
	private static final String LANG = "TURTLE";
	private final RDFStore rdfStore = RDFStore.getInstance();
	protected static final Query SYSTEM_QUERY = QueryFactory
			.create("prefix saref: <http://ontology.tno.nl/saref#> "
					+ "SELECT ?uri_system ?state where{ ?uri_system saref:hasState ?state }");
	protected static final String QUERY_UPDATE_STATE_SYSTEM = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "DELETE { ${URI_SYSTEM} saref:hasState ?x } "
			+ "INSERT { ${URI_SYSTEM} saref:hasState ${STATE} } "
			+ "WHERE { ${URI_SYSTEM} saref:hasState ?x }";

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
				logger.info("Update " + message);
				
				// rdfStore.save(description);
				//rdfStore.select(query); // получить текущее состояние
				// можно упростить если посылать просто uri системы без состояния
				
				QueryExecution qe = QueryExecutionFactory.create(SYSTEM_QUERY,
						description);
				ResultSet systems = qe.execSelect();

				while (systems.hasNext()) {
					QuerySolution qs = systems.next();
					String uriSystem = qs.getResource("uri_system").getURI();
					String state = qs.getResource("state").getURI();
					if(uriSystem != null && state != null) {
					rdfStore.update(QUERY_UPDATE_STATE_SYSTEM.replace("${URI_SYSTEM}", uriSystem)
							.replace("${STATE}", state)); 
					}
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

}
