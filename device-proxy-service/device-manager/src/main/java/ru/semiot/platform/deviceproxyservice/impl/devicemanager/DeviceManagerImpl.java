package ru.semiot.platform.deviceproxyservice.impl.devicemanager;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class DeviceManagerImpl implements DeviceManager {

    public void register(Device device) {
        System.out.println("Register device [ID=" + device.getID() + "]");
    }

}
