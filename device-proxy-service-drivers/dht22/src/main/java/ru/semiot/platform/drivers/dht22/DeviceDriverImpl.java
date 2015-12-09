package ru.semiot.platform.drivers.dht22;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class DeviceDriverImpl implements DeviceDriver, ManagedService {

    private static final String IP_KEY = Activator.PID + ".ip";
    private static final String PORT_KEY = Activator.PID + ".port";
    private static final String LAT_KEY = Activator.PID + ".lat";
    private static final String LNG_KEY = Activator.PID + ".lng";
            
    private final List<Device> listDevices = Collections.synchronizedList(new ArrayList<Device>());
    
    public static final String templateOffState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<http://${DOMAIN}/${SYSTEM_PATH}/${DEVICE_HASH}> saref:hasState saref:OffState.";
    
    private ScheduledExecutorService scheduler;
	private ScheduledDevice scheduledDevice;
	private ScheduledFuture handle = null;
	private final String driverName = "dht22";
	private CoAPInterface coap;
	
    private volatile DeviceManager deviceManager;

    private String templateDescription;
    private String templateObservationTemperature;
    private String templateObservationHumidity;

    private int port = 5683;
    private String ip = "94.19.230.213";
    private String lat = "60.01352";
    private String lng = "30.287799";

    public List<Device> listDevices() {
        return listDevices;
    }

    public void start() {
        System.out.println("Dht22 temperature driver started!");

        readTemplates();

        coap = new CoAPInterface();
        
        this.scheduler = Executors.newScheduledThreadPool(1);
		this.scheduledDevice = new ScheduledDevice(this);
		this.scheduledDevice.createCoapClients();
		startSheduled();
    }

    public void stop() {
        // перевод всех устройств в статус офф
        stopSheduled();
        coap.stop();

        for (Device device : listDevices)
		{
        	if(device.getTurnOn()) {
	        	System.out.println(templateOffState.replace("${DOMAIN}", getDomain())
	        			.replace("${SYSTEM_PATH}", getPathSystemUri())
	        			.replace("${DEVICE_HASH}", device.getID()));
	        	inactiveDevice(templateOffState.replace("${DOMAIN}", getDomain())
	        			.replace("${SYSTEM_PATH}", getPathSystemUri())
	        			.replace("${DEVICE_HASH}", device.getID()));
        	}
		}
        
        System.out.println("Dht22 temperature driver stopped!");
    }

    public void updated(Dictionary properties) throws ConfigurationException {
        synchronized(this) {
            System.out.println(properties == null);
            if(properties != null) {
            	ip = (String) properties.get(IP_KEY);
            	port = (Integer) properties.get(PORT_KEY);
            	lat = (String) properties.get(LAT_KEY);
            	lng = (String) properties.get(LNG_KEY);
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

    public String getTemplateDescription() {
        return templateDescription;
    }

    public String getTemplateObservationTemperature() {
        return templateObservationTemperature;
    }
    
    public String getTemplateObservationHumidity() {
        return templateObservationHumidity;
    }
    
    public int getPort() {
		return port;
	}
    
    public String getIp() {
		return ip;
	}
    
    public String getLat() {
    	return lat;
    }
    
    public String getLng() {
    	return lng;
    }
    
    public String getDomain() {
    	return deviceManager.getDomain();
    }
    
    public String getPathSystemUri() {
    	return deviceManager.getPathSystemUri();
    }
    
    public String getPathSensorUri() {
    	return deviceManager.getPathSensorUri();
    }
    
    public String getDriverName() {
		return driverName;
	}
    
    public CoAPInterface getCoap() {
    	return coap;
    }
    
    private void readTemplates() {
        try {
            this.templateDescription = IOUtils.toString(DeviceDriverImpl.class
                    .getResourceAsStream("/ru/semiot/platform/drivers/dht22/description.ttl"));
            this.templateObservationTemperature = IOUtils.toString(DeviceDriverImpl.class
                    .getResourceAsStream("/ru/semiot/platform/drivers/dht22/observationTemperature.ttl"));
            this.templateObservationHumidity = IOUtils.toString(DeviceDriverImpl.class
                    .getResourceAsStream("/ru/semiot/platform/drivers/dht22/observationHumidity.ttl"));
        } catch (IOException ex) {
            System.out.println("Can't read templates");
            throw new IllegalArgumentException(ex);
        }
    }
    
    // TODO
    public void restartSheduller() {
    	/*stopSheduled();
    	try {
			Thread.currentThread().sleep(1000 * 60 * 20);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
		}
    	this.scheduledDevice.createCoapClients();
    	startSheduled();*/
    }
    
    public void startSheduled() {
		if (this.handle != null)
			stopSheduled();

		int nDelay = 60;
		this.handle = this.scheduler.scheduleAtFixedRate(this.scheduledDevice,
				1, nDelay, TimeUnit.SECONDS); // Minutes
		System.out.println("UScheduled started. Repeat will do every "
				+ String.valueOf(nDelay) + " seconds");
	}

	public void stopSheduled() {
		if (handle == null)
			return;

		handle.cancel(true);
		handle = null;
		System.out.println("UScheduled stoped");
	}

}
