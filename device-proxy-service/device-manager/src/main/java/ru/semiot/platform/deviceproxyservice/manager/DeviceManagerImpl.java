package ru.semiot.platform.deviceproxyservice.manager;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceStatus;
import rx.Observable;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;

public class DeviceManagerImpl implements DeviceManager, ManagedService {

	private static final Logger logger = LoggerFactory.getLogger(DeviceManagerImpl.class);
	
	private static final String TOPIC_NEWANDOBSERVING_KEY = "ru.semiot.platform.deviceproxyservice.manager.topic_newandobserving";
	private static final String TOPIC_INACTIVE = "ru.semiot.platform.deviceproxyservice.manager.topic_inactive";
	private static final String WAMP_URI_KEY = "ru.semiot.platform.deviceproxyservice.manager.wamp_uri";
	private static final String WAMP_REALM_KEY = "ru.semiot.platform.deviceproxyservice.manager.wamp_realm";
	private static final String WAMP_RECONNECT_KEY = "ru.semiot.platform.deviceproxyservice.manager.wamp_reconnect";
	private static final String FUSEKI_PASSWORD="ru.semiot.platform.deviceproxyservice.manager.fuseki_password";
	private static final String FUSEKI_USERNAME="ru.semiot.platform.deviceproxyservice.manager.fuseki_username";
	private static final String FUSEKI_UPDATE_URL="ru.semiot.platform.deviceproxyservice.manager.fuseki_update_url";
	private static final String FUSEKI_QUERY_URL="ru.semiot.platform.deviceproxyservice.manager.fuseki_query_url";
	private static final String FUSEKI_STORE_URL="ru.semiot.platform.deviceproxyservice.manager.fuseki_store_url";
	private static final String DOMAIN = "ru.semiot.platform.deviceproxyservice.manager.domain";
	private static final String PATH_SYSTEM_URI = "ru.semiot.platform.deviceproxyservice.manager.system_path";
	private static final String PATH_SENSOR_URI = "ru.semiot.platform.deviceproxyservice.manager.sensor_path";
    
    private String wampUri = "ws://wamprouter:8080/ws";
    private String wampRealm = "realm1";
    private int wampReconnect = 15;
    private String topicNewAndObserving = "ru.semiot.devices.newandobserving";
    private String topicInactive = "ru.semiot.devices.turnoff";
    private String fusekiPassword = "pw";
    private String fusekiUsername = "admin";
    private String fusekiUpdateUrl = "http://localhost:3030/ds/update";
    private String fusekiQueryUrl = "http://localhost:3030/ds/query";
    private String fusekiStoreUrl = "http://localhost:3030/ds/data";
    private String domain = "localhost";
    private String pathSystemUri = "systems";
    private String pathSensorUri = "sensors";

    private DirectoryService directoryService;
    
    public void start() {
    	directoryService = new DirectoryService(this);
    	logger.info(wampUri);
        try {
            WAMPClient
                    .getInstance()
                    .init(wampUri, wampRealm, wampReconnect)
                    .subscribe(
                            (WampClient.Status newStatus) -> {
                                if (newStatus == WampClient.Status.Connected) {
                                	logger.info("Connected to {}", wampUri);
                                } else if (newStatus == WampClient.Status.Disconnected) {
                                	logger.info("Disconnected from {}",	wampUri);
                                } else if (newStatus == WampClient.Status.Connecting) {
                                	logger.info("Connecting to {}", wampUri);
                                }
                            });
            logger.info("Device Proxy Service Manager started!");
        } catch (ApplicationError ex) {
        	logger.error(ex.getMessage(), ex);
            try {
                WAMPClient.getInstance().close();
            } catch (IOException ex1) {
            	logger.error(ex1.getMessage(), ex1);
            }
        }
    }

    public void stop() {
        try {
            WAMPClient.getInstance().close();
            directoryService = null;
        } catch (IOException ex) {
        	logger.error(ex.getMessage(), ex);
        }
        logger.info("Device Proxy Service Manager stopped!");
    }

    @Override
    public void updated(Dictionary properties) throws ConfigurationException {
        synchronized (this) {
            if (properties != null) {
            	if (properties.get(WAMP_URI_KEY) != null) {
            		wampUri = (String) properties.get(WAMP_URI_KEY);
            	}
            	if (properties.get(WAMP_REALM_KEY) != null) {
            		wampRealm = (String) properties.get(WAMP_REALM_KEY);
            	}
                if (properties.get(WAMP_RECONNECT_KEY) != null) {
                    wampReconnect = (int) properties.get(WAMP_RECONNECT_KEY);
                }
                if (properties.get(TOPIC_NEWANDOBSERVING_KEY) != null) {
                	topicNewAndObserving = (String) properties.get(TOPIC_NEWANDOBSERVING_KEY);
                }
                if (properties.get(TOPIC_INACTIVE) != null) {
                	topicInactive = (String) properties.get(TOPIC_INACTIVE);
                }
                if(properties.get(FUSEKI_PASSWORD) != null) {
                	fusekiPassword = (String) properties.get(FUSEKI_PASSWORD);
                }
                if (properties.get(FUSEKI_USERNAME) != null) {
                	fusekiUsername = (String) properties.get(FUSEKI_USERNAME);
                }
                if (properties.get(FUSEKI_UPDATE_URL) != null) {
                	fusekiUpdateUrl = (String) properties.get(FUSEKI_UPDATE_URL);
                }
                if (properties.get(FUSEKI_QUERY_URL) != null) {
                	fusekiQueryUrl = (String) properties.get(FUSEKI_QUERY_URL);
                }
                if (properties.get(FUSEKI_STORE_URL) != null) {
                	fusekiStoreUrl = (String) properties.get(FUSEKI_STORE_URL);
                }
                if (properties.get(PATH_SYSTEM_URI) != null) {
                	pathSystemUri = (String) properties.get(PATH_SYSTEM_URI);
                }
                if (properties.get(PATH_SENSOR_URI) != null) {
                	pathSensorUri = (String) properties.get(PATH_SENSOR_URI);
                }
                
                domain = (String) properties.get(DOMAIN);
            }
        }
    }

   

    @Override
    public void updateStatus(Device device, DeviceStatus status) {
       logger.info("Update device status [ID={};status={}]", device.getID(), status);
    }

    @Override
    public void publish(String topic, String message) {
        WAMPClient.getInstance().publish(topic, message);
    }

    @Override
    public Observable<String> subscribe(String topic) {
        return WAMPClient.getInstance().subscribe(topic);
    }
    
    @Override
    public void inactiveDevice(String message) {
    	if(directoryService != null) {
    		directoryService.inactiveDevice(message);
    		publish(topicInactive, message);
    	} else {
    		logger.error("DirectoryService has not been initialized");
    	}
    }
    
    @Override
    public void register(Device device) {
    	if(directoryService != null) {
    		logger.info("Register device [ID={}]", device.getID());
    		directoryService.registerDevice(device.getRDFDescription());
    	} else {
    		logger.error("DirectoryService has not been initialized");
    	}
    }
    
    public String getTopicNewAndObserving() {
    	return topicNewAndObserving;
    }
    
    public String getTopicInactive() {
    	return topicInactive;
    }
    
    public String getFusekiPassword() {
    	return fusekiPassword;
    }
    
    public String getFusekiUsername() {
    	return fusekiUsername;
    }
    
    public String getFusekiUpdateUrl() {
    	return fusekiUpdateUrl;
    }
    
    public String getFusekiQueryUrl() {
    	return fusekiQueryUrl;
    }
    
    public String getFusekiStoreUrl() {
    	return fusekiStoreUrl;
    }
    
    public String getDomain() {
    	return domain;
    }

	public String getPathSystemUri() {
		return pathSystemUri;
	}
	
	public String getPathSensorUri() {
		return pathSensorUri;
	}
    


}
