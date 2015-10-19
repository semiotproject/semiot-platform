package ru.semiot.platform.drivers.simulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;
import ru.semiot.platform.drivers.simulator.Activator;

public class DeviceDriverImpl implements DeviceDriver, ManagedService {

	private volatile DeviceManager deviceManager;
	private final List<Device> listDevices = Collections
			.synchronizedList(new ArrayList<Device>());
	private static final String templateOffState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<${system}> saref:hasState saref:OffState.";

	CoAPInterface coap;
	// properties
	private static final String PORT_KEY = Activator.SERVICE_PID + ".port";
	private static final String WAMP_MESSAGE_FORMAT = Activator.SERVICE_PID
			+ ".wamp_message_format";
	private static final String TOPIC_INACTIVE = Activator.SERVICE_PID
			+ ".topic_inactive";

	private int port;
	private String wampMessageFormat;
	private String topicInactive;

	public List<Device> listDevices() {
		return listDevices;
	}

	public void start() {
		try {
			coap = new CoAPInterface(this);
			coap.start();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public void stop() {
		try {
			coap.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		
		for (Device device : listDevices)
		{
        	publish(topicInactive, templateOffState.replace("${system}", device.getID()));
		}
        
        System.out.println("Simulator driver stopped!");
	}

	public void updated(Dictionary properties) throws ConfigurationException {
		synchronized (this) {
			System.out.println(properties == null);
			if (properties != null) {
				port = (Integer) properties.get(PORT_KEY);
				wampMessageFormat = (String) properties
						.get(WAMP_MESSAGE_FORMAT);
				topicInactive = (String) properties.get(TOPIC_INACTIVE);
			}
		}
	}

	public void publish(String topic, String message) {
		deviceManager.publish(topic, message);
	}

	public void addDevice(Device device) {
		listDevices.add(device);
		deviceManager.register(device);
	}

	public boolean contains(Device device) {
		return listDevices.contains(device);
	}

	public int getPort() {
		return port;
	}

	public String getWampMessageFormat() {
		return wampMessageFormat;
	}

	public String getTopicInactive() {
		return topicInactive;
	}

}
