package ru.semiot.platform.deviceproxyservice.manager;

import java.io.IOException;
import java.util.Dictionary;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceStatus;
import rx.Observable;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;

public class DeviceManagerImpl implements DeviceManager, ManagedService {

    private static final String TOPIC_REGISTER_KEY = "ru.semiot.platform.deviceproxyservice.manager.topic_register";
    private static final String TOPIC_NEWANDOBSERVING_KEY = "ru.semiot.platform.deviceproxyservice.manager.topic_newandobserving";
    private static final String WAMP_URI_KEY = "ru.semiot.platform.deviceproxyservice.manager.wamp_uri";
    private static final String WAMP_REALM_KEY = "ru.semiot.platform.deviceproxyservice.manager.wamp_realm";
    private static final String WAMP_RECONNECT_KEY = "ru.semiot.platform.deviceproxyservice.manager.wamp_reconnect";

    private String wampUri;
    private String wampRealm;
    private int wampReconnect = 15;
    private String topicRegister;
    private String topicNewAndObserving;

    public void start() {
        try {
            WAMPClient
                    .getInstance()
                    .init(wampUri, wampRealm, wampReconnect)
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
            
            System.out.println("Device Proxy Service Manager started!");
        } catch (ApplicationError ex) {
            System.out.println(ex.getMessage());
            try {
                WAMPClient.getInstance().close();
            } catch (IOException ex1) {
                System.out.println(ex1.getMessage());
            }
        }
    }

    public void stop() {
        try {
            WAMPClient.getInstance().close();
        } catch (IOException ex1) {
            System.out.println(ex1.getMessage());
        }
        System.out.println("Device Proxy Service Manager stopped!");
    }

    @Override
    public void updated(Dictionary properties) throws ConfigurationException {
        synchronized (this) {
            if (properties != null) {
                wampUri = (String) properties.get(WAMP_URI_KEY);
                wampRealm = (String) properties.get(WAMP_REALM_KEY);
                if (properties.get(WAMP_RECONNECT_KEY) != null) {
                    wampReconnect = (int) properties.get(WAMP_RECONNECT_KEY);
                }
                topicRegister = (String) properties.get(TOPIC_REGISTER_KEY);
                topicNewAndObserving = (String) properties.get(TOPIC_NEWANDOBSERVING_KEY);
            }
        }
    }

    @Override
    public void register(Device device) {
        System.out.println("Register device [ID=" + device.getID() + "]");
        publish(topicNewAndObserving, device.getRDFDescription());
        publish(topicRegister, device.getRDFDescription());
    }

    @Override
    public void updateStatus(Device device, DeviceStatus status) {
        System.out.println(
                "Update device status [ID=" + device.getID() + ";status=" + status + "]");
    }

    @Override
    public void publish(String topic, String message) {
        WAMPClient.getInstance().publish(topic, message);
    }

    @Override
    public Observable<String> subscribe(String topic) {
        return WAMPClient.getInstance().subscribe(topic);
    }

}
