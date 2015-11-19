package ru.semiot.platform.drivers.simulator.handlers.coap;

import org.apache.jena.riot.RiotException;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.platform.drivers.simulator.DeviceDriverImpl;
import ru.semiot.platform.drivers.simulator.handlers.coap.DeviceHandler;

public class NewObservationHandler implements CoapHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(NewObservationHandler.class);
	
	private static final String templateOffState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<${system}> saref:hasState saref:OffState.";
	private final String topic;
	private final String system;
	private final String coapUri;
	private DeviceDriverImpl deviceDriverImpl;

	private CoapObserveRelation relation;

	public NewObservationHandler(DeviceDriverImpl deviceDriverImpl,
			final String topic, final String system, final String coapUri) {
		this.deviceDriverImpl = deviceDriverImpl;
		this.topic = topic; // hash 
		this.system = system; // TODO оставить только hash, сейчас оставлен и хэш и systemUri, т.к. домэин может измениться и изменится uri
		this.coapUri = coapUri;
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
				deviceDriverImpl.publish(topic, response.getResponseText());
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		} else {
			logger.info("Received unexpected response: {} {}", response.getCode(), response.getResponseText());
		}
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
	
	public String getCoapUri() {
		return coapUri;
	}
}
