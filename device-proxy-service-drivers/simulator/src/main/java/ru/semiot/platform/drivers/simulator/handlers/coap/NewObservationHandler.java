package ru.semiot.platform.drivers.simulator.handlers.coap;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.RiotException;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import ru.semiot.platform.drivers.simulator.DeviceDriverImpl;
import ru.semiot.platform.drivers.simulator.handlers.coap.DeviceHandler;
import ru.semiot.platform.drivers.simulator.resources.RegisterResource;
import ru.semiot.semiot.commons.namespaces.DCTERMS;

public class NewObservationHandler implements CoapHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(NewObservationHandler.class);
	
	private static final String QUERY_URI_SENSOR = "prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> "
			+ "select ?y { ?x ssn:observedBy ?y. }";
	private static final String templateOffState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<${system}> saref:hasState saref:OffState.";
	private final String topic;
	private final String system;
	private final Resource sensor;
	private final int sensor_id;
	private DeviceDriverImpl deviceDriverImpl;

	private CoapObserveRelation relation;

	public NewObservationHandler(DeviceDriverImpl deviceDriverImpl,
			final String topic, final String system, final Resource sensor, int sensor_id) {
		this.deviceDriverImpl = deviceDriverImpl;
		this.topic = topic; // hash 
		this.system = system; // TODO оставить только hash, сейчас оставлен и хэш и systemUri, т.к. домэин может измениться и изменится uri
		this.sensor = sensor;
		this.sensor_id = sensor_id;
	}

	public void setRelation(final CoapObserveRelation relation) {
		this.relation = relation;
	}

	@Override
	public void onLoad(CoapResponse response) {
		if (response.getCode() == CoAP.ResponseCode.NOT_FOUND) {
			inactiveSystem("Path doesn't exist. Cancel subscription!");
		} else if (response.getCode() == CoAP.ResponseCode.CONTENT) {
			try {
				String respText = response.getResponseText();
				Model model = toModel(respText);
				/* try (QueryExecution qexec = QueryExecutionFactory.create(QUERY_URI_SENSOR,
						model)) {
					final ResultSet results = qexec.execSelect();
					while (results.hasNext()) {
						final QuerySolution soln = results.next();// uri sensor не заменяется!

						final Resource uri_sensor = soln.getResource("y");
					}
				}*/
				final Literal sensorIdLiteral = ResourceFactory
						.createTypedLiteral(topic + "-" + String.valueOf(sensor_id));
				model.add(sensor, DCTERMS.identifier, sensorIdLiteral);
				String obs = toString(model);
				obs = obs.replace(sensor.getURI(), RegisterResource.templateSensorUri
						.replace("${DOMAIN}", deviceDriverImpl.getDomain())
						.replace("${SENSOR_PATH}", deviceDriverImpl.getPathSensorUri())
						.replace("${DEVICE_HASH}", topic)
						.replace("${SENSOR_ID}", String.valueOf(sensor_id)));
				
				deviceDriverImpl.publish(topic, obs);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		} else {
			logger.info("Received unexpected response: {} {}", response.getCode(), response.getResponseText());
		}
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
	
	private Model toModel(final String payload) {
		return ModelFactory.createDefaultModel().read(
				IOUtils.toInputStream(payload), null,
				deviceDriverImpl.getWampMessageFormat());
	}

	@Override
	public void onError() {
		inactiveSystem("Something went wrong! Stop proxying to ");
	}

	public void stopProxying(String warnMessage) {
		System.out.println(warnMessage + topic);
		relation.reactiveCancel();
	}

	private synchronized void inactiveSystem(String warnMessage) {
		try {
			if (!DeviceHandler.getInstance().emptyHandlersInDevice(system)) {
				DeviceHandler.getInstance().inactiveDevice(system, warnMessage);
				String message = templateOffState.replace("${system}", system);
				System.out.println("Inactive device message: "
						+ message);
				deviceDriverImpl.inactiveDevice(message);
			}
		} catch (RiotException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
	
	public String getTopic() {
		return topic;
	}
	
	public String getSystemUri() {
		return system;
	}
	
	public int getSensorId() {
		return sensor_id;
	}
}
