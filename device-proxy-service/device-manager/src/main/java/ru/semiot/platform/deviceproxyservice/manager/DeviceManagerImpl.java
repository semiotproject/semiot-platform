package ru.semiot.platform.deviceproxyservice.manager;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceStatus;

public class DeviceManagerImpl implements DeviceManager {

    public void register(Device device) {
        System.out.println("Register device [ID=" + device.getID() + "]");
    }

    public void updateStatus(Device device, DeviceStatus status) {
        System.out.println(
                "Update device status [ID=" + device.getID() + ";status=" + status + "]");
    }

}
