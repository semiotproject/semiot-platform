package ru.semiot.platform.drivers.simulator.resources;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileManager;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.riot.RiotException;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.drivers.simulator.CoAPInterface;
import ru.semiot.platform.drivers.simulator.DeviceDriverImpl;
import ru.semiot.platform.drivers.simulator.handlers.coap.DeviceHandler;
import ru.semiot.platform.drivers.simulator.handlers.coap.NewObservationHandler;
import ru.semiot.semiot.commons.namespaces.DCTERMS;
import ru.semiot.semiot.commons.namespaces.EMTR;
import ru.semiot.semiot.commons.namespaces.HMTR;
import ru.semiot.semiot.commons.namespaces.RDF;
import ru.semiot.semiot.commons.namespaces.SSN;
import ru.semiot.semiot.commons.namespaces.SSNCOM;

public class RegisterResource extends CoapResource {
	
	private static final Logger logger = LoggerFactory.getLogger(RegisterResource.class);

	private static final String templateWampURI = "ws://wamprouter/ws?topic=${topic}";
	private static final String templateWampTopic = "\"${topic}\"^^xsd:string";
	
	private static final String queryFile = "/ru/semiot/services/deviceproxy/handlers/wamp/NewDeviceHandler/query.sparql";
	private static final String templateOnState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<${system}> saref:hasState saref:OnState.";
	private static final String templateSystemUri = "http://${DOMAIN}/${PATH}/${DEVICE_HASH}";
	private final Model schema;
	private final Query query;
	private static final String VAR_COAP = "coap";
	private static final String VAR_WAMP = "wamp";
	private static final String VAR_SYSTEM = "system";
	private static final String WAMP = "WAMP";
	DeviceDriverImpl deviceDriverImpl;
	
	private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;

	public RegisterResource(DeviceDriverImpl deviceDriverImpl) {
		super("register");
		logger.info("Register resoure");
		this.deviceDriverImpl = deviceDriverImpl;

		this.schema = FileManager.get().loadModel(SSN.URI);
		this.schema.add(FileManager.get().loadModel(HMTR.URI));
		this.schema.add(FileManager.get().loadModel(EMTR.URI));
		try {
			this.query = QueryFactory.create(IOUtils.toString(this.getClass()
					.getResourceAsStream(queryFile)));
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		Model description = toModel(exchange.getRequestText());

		if (!description.isEmpty()) {
			addDevice(description);

			exchange.respond(CoAP.ResponseCode.CREATED);
		} else {
			logger.info("Received a request without payload or in wrong format");
			exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
		}
	}

	private void addDevice(Model description) {
		try {
			if (!description.isEmpty()) {
				InfModel infModel = ModelFactory.createRDFSModel(schema,
						description);

				try (QueryExecution qexec = QueryExecutionFactory.create(query,
						infModel)) {
					final ResultSet results = qexec.execSelect();

					String systemURI = StringUtils.EMPTY; // с расчетом,
															// что в 1
															// сообщении 1
															// устройство
					String hash = StringUtils.EMPTY;
					Resource system = null;
					boolean containsHandler = false;
					while (results.hasNext()) {
						final QuerySolution soln = results.next();

						final Resource coap = soln.getResource(VAR_COAP);
						if (systemURI.isEmpty()) {
							system = soln.getResource(VAR_SYSTEM);
							hash = getHash(system.getURI());
							systemURI = templateSystemUri.replace("${DOMAIN}", deviceDriverImpl.getDomain())
									.replace("${DEVICE_HASH}", hash)
									.replace("${PATH}", deviceDriverImpl.getPathSystemUri());
						}
						
						containsHandler = DeviceHandler.getInstance().containsHandler(systemURI, coap.getURI());
						if(!containsHandler) {
							final NewObservationHandler handler = new NewObservationHandler(
									deviceDriverImpl, hash, systemURI, coap.getURI()); // можно оставить только hash
	
							final CoapClient coapClient = new CoapClient(
									coap.getURI());
							coapClient.setEndpoint(CoAPInterface.getEndpoint());
							final CoapObserveRelation rel = coapClient
									.observe(handler);
	
							// So the handler could cancel the subscription.
							handler.setRelation(rel);
							coapClient.shutdown();
							
							DeviceHandler.getInstance().addHandler(handler);
						}
					}
					if(system != null && !hash.isEmpty() && !systemURI.isEmpty()) {
						Device newDevice = new Device(systemURI, ""); // TODO отрефакторить!!!
						if(!deviceDriverImpl.contains(newDevice)) {
							String desc = toString(description
									.add(getModelEndpoint(system, hash)));
							newDevice.setRDFDescription(
									desc.replace("<" + system.getURI() + ">", "<" + systemURI + ">")); // TODO отрефакторить!!!
							
							deviceDriverImpl.addDevice(newDevice);
						} else if(!containsHandler) {
							deviceDriverImpl.inactiveDevice(
									templateOnState.replace("${system}", systemURI));
							logger.info("{} saref:OnState", systemURI);
						}
					}
				}
			} else {
				logger.warn("Received an empty message or in a wrong format!");
			}
		} catch (RiotException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
	
	private String getHash(String systemUri) {
		String name = systemUri + deviceDriverImpl.getDriverName();
		int h = FNV_32_INIT;
        final int len = name.length();
        for(int i = 0; i < len; i++) {
        	h ^= name.charAt(i);
        	h *= FNV_32_PRIME;
        }
        long longHash = h & 0xffffffffl;
        return String.valueOf(longHash);
	}

	@Override
	public void handleDELETE(CoapExchange exchange) {
		super.handleDELETE(exchange); // delete from list??
	}

	private Model toModel(final String payload) {
		return ModelFactory.createDefaultModel().read(
				IOUtils.toInputStream(payload), null,
				deviceDriverImpl.getWampMessageFormat());
	}

	private Model getModelEndpoint(Resource system, String hash) {
		final Model tmp = ModelFactory.createDefaultModel();

		final Resource wampEndpoint = ResourceFactory
				.createResource(templateWampURI.replace("${topic}",
						hash));
		final Literal hashLiteral = ResourceFactory
				.createTypedLiteral(hash);
	
		// Declare a new CommunicationEndpoint (WAMP)
		// tmp.setNsPrefix("xsd", "<http://www.w3.org/2001/XMLSchema#>");
		tmp.add(system, SSNCOM.hasCommunicationEndpoint, wampEndpoint)
				.add(system, DCTERMS.identifier, hashLiteral)
				.add(wampEndpoint, RDF.type,
						SSNCOM.CommunicationEndpoint)
				.add(wampEndpoint, SSNCOM.topic, hashLiteral)
				.add(wampEndpoint, SSNCOM.protocol, WAMP);
		
		return tmp;
	}

	private String coapUriToWAMPUri(final String coapUri) {
		return coapUri.replaceAll("coap://", "").replaceAll(":|/", ".");
	}

	private String toString(final Model model) {
		try (Writer writer = new StringWriter()) {
			model.write(writer, deviceDriverImpl.getWampMessageFormat());
			return writer.toString();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}

	private String getWampTopic(final String wampUri) {
		return wampUri.split("\\?topic=")[1];
	}
}
