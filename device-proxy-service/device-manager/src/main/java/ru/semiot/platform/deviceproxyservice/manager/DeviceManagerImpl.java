package ru.semiot.platform.deviceproxyservice.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceStatus;
import rx.Observable;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;

public class DeviceManagerImpl implements DeviceManager {
	// Убрать в конфиг
	private final String topicRegister = "ru.semiot.devices.register";
	private final String topicsNewAndObserving = "ru.semiot.devices.newandobserving";
	private final String wampUri = "ws://demo.semiot.ru:8080/ws";
	private final String wampRealm = "realm1"; 
	private final int wampReconnectInterval = 15;
	
    public void start() {
        System.out.println("Init WAMPClient!");
        initWampRouter();
    }
    
    public void stop() {
    	try {
			WAMPClient.getInstance().close();
		} catch (IOException ex1) {
			System.out.println(ex1.getMessage());
		}
        System.out.println("DeviceDriverImplManager stopped!");
    }
    
    public void register(Device device) {
        System.out.println("Register device [ID=" + device.getID() + "]");
        publish(topicsNewAndObserving, device.getRDFDescription());
        publish(topicRegister, device.getRDFDescription());
    }

    public void updateStatus(Device device, DeviceStatus status) {
        System.out.println(
                "Update device status [ID=" + device.getID() + ";status=" + status + "]");
    }
    
    public void publish(String topic, String message) {
    	WAMPClient.getInstance().publish(topic, message);
    }
    
    public Observable<String> subscribe(String topic) {
    	return WAMPClient.getInstance().subscribe(topic);
    }
    
    private void initWampRouter() {
    	try {
			WAMPClient
					.getInstance()
					.init(wampUri, wampRealm,
							wampReconnectInterval)
					.subscribe(
							(WampClient.Status newStatus) -> {
								if (newStatus == WampClient.Status.Connected) {
									System.out.println("Connected to " + wampUri);

								} else if (newStatus == WampClient.Status.Disconnected) {
									System.out.println("Disconnected from " + wampUri);
								} else if (newStatus == WampClient.Status.Connecting) {
									System.out.println("Connecting to " + wampUri);
								}
							});
		} catch (ApplicationError ex) {
			System.out.println(ex.getMessage());
			try {
				WAMPClient.getInstance().close();
			} catch (IOException ex1) {
				System.out.println(ex1.getMessage());
			}
		}
    }

}
