package ru.semiot.platform.deviceproxyservice.manager;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.StringReader;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryService {
	
	private static final Logger logger = LoggerFactory
			.getLogger(DirectoryService.class);
	private static final String LANG = "TURTLE";
    private static final String QUERY_SELECT_SYSTEM = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "SELECT ?state where{ <${URI_SYSTEM}> saref:hasState ?state }";
    private static final String templateOnState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<${URI_SYSTEM}> saref:hasState saref:OnState.";
    protected static final Query SYSTEM_QUERY = QueryFactory
			.create("prefix saref: <http://ontology.tno.nl/saref#> "
			+ "SELECT ?uri_system ?state where{ ?uri_system saref:hasState ?state }");
	protected static final String QUERY_UPDATE_STATE_SYSTEM = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "DELETE { <${URI_SYSTEM}> saref:hasState ?x } "
			+ "INSERT { <${URI_SYSTEM}> saref:hasState ${STATE} } "
			+ "WHERE { <${URI_SYSTEM}> saref:hasState ?x }";
	
	DeviceManagerImpl dmi;
	RDFStore rdfStore;
	
	public DirectoryService(DeviceManagerImpl dmi) {
		this.dmi = dmi;
		rdfStore = new RDFStore(dmi);
	}
	
	public void inactiveDevice(String message) {
		String request = null;
		try {
			Model description = ModelFactory.createDefaultModel().read(
					new StringReader(message), null, LANG);
			if (!description.isEmpty()) {
				logger.info("Update " + message);
				
				QueryExecution qe = QueryExecutionFactory.create(SYSTEM_QUERY,
						description);
				ResultSet systems = qe.execSelect();
				
				while (systems.hasNext()) {
					QuerySolution qs = systems.next();
					String uriSystem = qs.getResource("uri_system").getURI();
					String state = qs.getResource("state").getURI();
					request = QUERY_UPDATE_STATE_SYSTEM.replace("${URI_SYSTEM}", uriSystem)
							.replace("${STATE}", state);
					if(uriSystem != null && state != null) {
						rdfStore.update(request); 
					}
				}
			} else {
				logger.warn("Received an empty message or in a wrong format!");
			}
		} catch (RiotException ex) {
			logger.warn(ex.getMessage(), ex);
		} catch (Exception ex) {
			logger.info("Exception with string: " + ((request!=null && !request.isEmpty())?request:"request message is empty"));
			logger.error(ex.getMessage(), ex);
		}
	}
    
    public void registerDevice(String message) {
    	try {
            Model description = ModelFactory.createDefaultModel()
                    .read(new StringReader(message), null, LANG);
            if (!description.isEmpty()) {
            	QueryExecution qe = QueryExecutionFactory.create(SYSTEM_QUERY, 
						description);
				ResultSet systems = qe.execSelect();

				while (systems.hasNext()) {
					QuerySolution qs = systems.next();
					String uriSystem = qs.getResource("uri_system").getURI();
					if (uriSystem != null) {
						ResultSet rs = rdfStore.select(QUERY_SELECT_SYSTEM.replace("${URI_SYSTEM}", uriSystem));
						if(rs.hasNext()) {
							rdfStore.update(QUERY_UPDATE_STATE_SYSTEM.replace("${URI_SYSTEM}", uriSystem)
									.replace("${STATE}", "saref:OnState"));
							// for interface
							WAMPClient.getInstance().publish(dmi.getTopicInactive(), 
									templateOnState.replace("${URI_SYSTEM}", uriSystem));
						} else {
							rdfStore.save(description);
							WAMPClient.getInstance().publish(dmi.getTopicNewAndObserving(), message);
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
