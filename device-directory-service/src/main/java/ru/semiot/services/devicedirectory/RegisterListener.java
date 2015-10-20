package ru.semiot.services.devicedirectory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import java.io.StringReader;

import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observer;

public class RegisterListener implements Observer<String> {

    private static final Logger logger = LoggerFactory.getLogger(
            RegisterListener.class);
    private static final String LANG = "TURTLE";
    private static final ServiceConfig config
            = ConfigFactory.create(ServiceConfig.class);
    private final RDFStore rdfStore = RDFStore.getInstance();
    private final WAMPClient wampClient = WAMPClient.getInstance();
    private final String QUERY_SELECT_SYSTEM = "prefix saref: <http://ontology.tno.nl/saref#> "
					+ "SELECT ?state where{ <${URI_SYSTEM}> saref:hasState ?state }";

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
            Model description = ModelFactory.createDefaultModel()
                    .read(new StringReader(message), null, LANG);
            if (!description.isEmpty()) {
                //TODO: check that the registering sensor doesn't exist already. Do we need it?

            	// проверка что устройства нет, если есть - смена статуса
            	// запрос поменять на нормальный
            	QueryExecution qe = QueryExecutionFactory.create(InactiveDeviceListener.SYSTEM_QUERY, 
						description);
				ResultSet systems = qe.execSelect();

				while (systems.hasNext()) {
					QuerySolution qs = systems.next();
					String uriSystem = qs.getResource("uri_system").getURI();
					if (uriSystem != null) {
						ResultSet rs = rdfStore.select(QUERY_SELECT_SYSTEM.replace("${URI_SYSTEM}", uriSystem));
						if(rs.hasNext()) {
							rdfStore.update(InactiveDeviceListener.QUERY_UPDATE_STATE_SYSTEM
									.replace("${URI_SYSTEM}", uriSystem).replace("${STATE}", "saref:OnState")); 
						}
						else {
							rdfStore.save(description);

			                wampClient.publish(config.topicsNewDevice(), message);
						}
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
