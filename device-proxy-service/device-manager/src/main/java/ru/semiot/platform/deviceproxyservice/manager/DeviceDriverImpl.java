package ru.semiot.platform.deviceproxyservice.manager;

import java.util.ArrayList;
import java.util.List;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;

public class DeviceDriverImpl implements DeviceDriver {

	List<Device> listDevices = new ArrayList<Device>();
	
	public DeviceDriverImpl() {
		
	}
	
	public boolean containsDevice(Device device) {
		return listDevices.contains(device);
	}
	
	public boolean containsDeviceId(String id) {
		return listDevices.contains(new Device(id, ""));
	}
	
	@Override
	public List<Device> listDevices() {
		return listDevices;
	}

}
