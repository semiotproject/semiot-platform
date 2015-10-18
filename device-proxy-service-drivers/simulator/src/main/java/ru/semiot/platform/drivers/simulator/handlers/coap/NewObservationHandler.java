package ru.semiot.platform.drivers.simulator.handlers.coap;

import org.apache.jena.riot.RiotException;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;

import ru.semiot.platform.drivers.simulator.DeviceDriverImpl;
import ru.semiot.platform.drivers.simulator.handlers.coap.DeviceHandler;

public class NewObservationHandler implements CoapHandler {
	private static final String templateOffState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<${system}> saref:hasState saref:OffState.";
	private final String topic;
	private final String system;
	private DeviceDriverImpl deviceDriverImpl;

	private CoapObserveRelation relation;

	public NewObservationHandler(DeviceDriverImpl deviceDriverImpl,
			final String topic, final String system) {
		this.deviceDriverImpl = deviceDriverImpl;
		this.topic = topic;
		this.system = system;
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
				System.out.println(ex.getMessage());
			}
		} else {
			System.out.println("Received unexpected response: "+response.getCode()+" " + response.getResponseText());
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
				System.out.println("Publish into topic "
						+ deviceDriverImpl.getTopicInactive() + " message: "
						+ message);
				deviceDriverImpl.publish(deviceDriverImpl.getTopicInactive(),
						message);
			}
		} catch (RiotException ex) {
			System.out.println(ex.getMessage());
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
}
