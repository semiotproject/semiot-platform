package ru.semiot.platform.drivers.simulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;
import ru.semiot.platform.drivers.simulator.Activator;

public class DeviceDriverImpl implements DeviceDriver, ManagedService {
	
	private static final Logger logger = LoggerFactory.getLogger(DeviceDriverImpl.class);

	private volatile DeviceManager deviceManager;
	private final List<Device> listDevices = Collections
			.synchronizedList(new ArrayList<Device>());
	private static final String templateOffState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<${system}> saref:hasState saref:OffState.";

	CoAPInterface coap;
	// properties
	private static final String PORT_KEY = Activator.PID + ".port";
	private static final String WAMP_MESSAGE_FORMAT = Activator.PID
			+ ".wamp_message_format";

	private int port = 3131;
	private String wampMessageFormat = "TURTLE";

	public List<Device> listDevices() {
		return listDevices;
	}

	public void start() {
		try {
			coap = new CoAPInterface(this);
			coap.start();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public void stop() {
		try {
			coap.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		
		
		for (Device device : listDevices)
		{
        	inactiveDevice(templateOffState.replace("${system}", device.getID()));
        	logger.info(templateOffState.replace("${system}", device.getID()));
		}
        
		logger.info("Simulator driver stopped!");
	}

	public void updated(Dictionary properties) throws ConfigurationException {
		synchronized (this) {
			System.out.println(properties == null);
			if (properties != null) {
				port = (Integer) properties.get(PORT_KEY);
				wampMessageFormat = (String) properties
						.get(WAMP_MESSAGE_FORMAT);
			}
		}
	}

	public void inactiveDevice(String message) {
    	deviceManager.inactiveDevice(message);
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

}
