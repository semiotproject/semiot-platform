package ru.semiot.platform.drivers.winghouse.machinetool;

import java.util.ArrayList;
import java.util.List;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class DeviceDriverImpl implements DeviceDriver {
    
    private volatile DeviceManager deviceManager;

    public List<Device> listDevices() {
        System.out.println("listDevices is called!");
        return new ArrayList<Device>();
    }
    
    public void start() {
        System.out.println("DeviceDriverImpl started!");
        deviceManager.register(new Device("0"));
    }
    
    public void stop() {
        System.out.println("DeviceDriverImpl stopped!");
    }
    
}
