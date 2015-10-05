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

	public void addHandler(String key, NewObservationHandler handler) {
		List<NewObservationHandler> handlers;
		if (observationHandlers.containsKey(key)) {
			handlers = observationHandlers.get(key);
		} else {
			handlers = new LinkedList<>();
			observationHandlers.put(key, handlers);
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

	public boolean containsHandler(String key, NewObservationHandler handler) {
		if (observationHandlers.containsKey(key)) {
			List<NewObservationHandler> handlers = observationHandlers.get(key);
			if (handlers.contains(handler)) {
				return true;
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
