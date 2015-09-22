package ru.semiot.platform.drivers.simulator;

import java.util.Dictionary;
import java.util.List;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class DeviceDriverImpl implements DeviceDriver, ManagedService {

	private volatile DeviceManager deviceManager;
	
	public void updated(Dictionary properties) throws ConfigurationException {

	}

	public List<Device> listDevices() {
		return null;
	}

}
