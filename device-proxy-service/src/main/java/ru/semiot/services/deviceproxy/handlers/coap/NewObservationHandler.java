package ru.semiot.services.deviceproxy.handlers.coap;

import org.apache.jena.riot.RiotException;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.services.deviceproxy.Launcher;
import ru.semiot.services.deviceproxy.WAMPClient;
import ru.semiot.services.deviceproxy.handlers.wamp.DeviceHandler;

public class NewObservationHandler implements CoapHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(NewObservationHandler.class);
	private final String topic;
	private final String system;
	private final WAMPClient wampClient = WAMPClient.getInstance();
	private CoapObserveRelation relation;

	public NewObservationHandler(final String topic, final String system) {
		this.topic = topic;
		this.system = system;
	}

	public void setRelation(final CoapObserveRelation relation) {
		this.relation = relation;
	}

	@Override
	public void onLoad(CoapResponse response) {
		if (response.getCode() == CoAP.ResponseCode.NOT_FOUND) {
			inactiveSystem("Path {} doesn't exist. Cancel subscription!");
		} else if (response.getCode() == CoAP.ResponseCode.CONTENT) {
			try {
				wampClient.publish(topic, response.getResponseText());
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		} else {
			logger.warn("Received unexpected response: {} {}",
					response.getCode(), response.getResponseText());
		}
	}

	@Override
	public void onError() {
		inactiveSystem("Something went wrong! Stop proxying to {} topic");
	}

	public void stopProxying(String warnMessage) {
		logger.warn(warnMessage, topic);
		relation.reactiveCancel();
	}

	private synchronized void inactiveSystem(String warnMessage) {
		try {
			if (!DeviceHandler.getInstance().emptyHandlersInDevice(system)) {
				DeviceHandler.getInstance().inactiveDevice(system, warnMessage);
				String message = Launcher.getConfig().mappingToOffState(system);
				logger.info("Publish into topic "
						+ Launcher.getConfig().topicsInactive() + " message: "
						+ message);
				wampClient.publish(Launcher.getConfig().topicsInactive(),
						message);
			}
		} catch (RiotException ex) {
			logger.warn(ex.getMessage(), ex);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
}
