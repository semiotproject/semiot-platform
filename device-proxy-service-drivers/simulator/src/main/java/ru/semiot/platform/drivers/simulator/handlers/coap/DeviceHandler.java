package ru.semiot.platform.drivers.simulator.handlers.coap;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DeviceHandler {
	private Map<String, List<NewObservationHandler>> observationHandlers = new HashMap<>();
	private static volatile DeviceHandler instance = null;

	public static DeviceHandler getInstance() {
		if (instance == null)
			synchronized (DeviceHandler.class) {
				if (instance == null)
					instance = new DeviceHandler();
			}
		return instance;
	}
	
	public void addHandler(NewObservationHandler handler) {
		List<NewObservationHandler> handlers;
		if (observationHandlers.containsKey(handler.getSystemUri())) {
			handlers = observationHandlers.get(handler.getSystemUri());
		} else {
			handlers = new LinkedList<>();
			observationHandlers.put(handler.getSystemUri(), handlers);
		}
		handlers.add(handler);
	}

	public void inactiveDevice(String key, String message) {
		List<NewObservationHandler> handlers = observationHandlers.get(key);
		if (handlers != null) {
			for (NewObservationHandler handler : handlers) {
				handler.stopProxying(message);
			}
			handlers.clear();
		}
	}

	public void removeDevice(String key) {
		inactiveDevice(key, "Device is removed! Stop proxying to ");
		observationHandlers.remove(key);
	}

	public boolean containsHandler(String key, String wampTopic) {
		if (observationHandlers.containsKey(key) && wampTopic != null) {
			List<NewObservationHandler> handlers = observationHandlers.get(key);
			for(NewObservationHandler handler : handlers) {
				if (wampTopic.equals(handler.getTopic())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean emptyHandlersInDevice(String key) {
		if (observationHandlers.containsKey(key)
				&& observationHandlers.get(key).isEmpty()) {
			return true;
		}
		return false;
	}

	public boolean containsDevice(String key) {
		return observationHandlers.containsKey(key);
	}
}
