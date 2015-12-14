package ru.semiot.platform.drivers.narodmon.temperature;

import static java.util.concurrent.TimeUnit.MINUTES;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.standard.SheetCollate;

import org.apache.commons.io.IOUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class DeviceDriverImpl implements DeviceDriver, ManagedService {

    private static final String URL = Activator.PID + ".url";
    private static final String SCHEDULED_DELAY = Activator.PID + ".scheduled_delay";
    private final List<Device> listDevices = Collections.synchronizedList(new ArrayList<Device>());
    
    public static final String templateOffState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<http://${DOMAIN}/${SYSTEM_PATH}/${DEVICE_HASH}> saref:hasState saref:OffState.";
    
    private ScheduledExecutorService scheduler;
	private ScheduledDevice scheduledDevice;
	private ScheduledFuture handle = null;
	private final String driverName = "Narodmon temperature";
    
    private volatile DeviceManager deviceManager;

    private String templateDescription;
    private String templateObservation;

    private String url = "http://narodmon.ru";
    private int scheduledDelay = 10;

    public List<Device> listDevices() {
        return listDevices;
    }

    public void start() {
        System.out.println("Narodmon temperature driver started!");

        readTemplates();
        this.scheduler = Executors.newScheduledThreadPool(1);
		this.scheduledDevice = new ScheduledDevice(this);
		startSheduled();
    }

    public void stop() {
        // перевод всех устройств в статус офф
        stopSheduled();

        for (Device device : listDevices)
		{
        	System.out.println(templateOffState.replace("${DOMAIN}", getDomain())
        			.replace("${SYSTEM_PATH}", getPathSystemUri())
        			.replace("${DEVICE_HASH}", device.getID()));
        	inactiveDevice(templateOffState.replace("${DOMAIN}", getDomain())
        			.replace("${SYSTEM_PATH}", getPathSystemUri())
        			.replace("${DEVICE_HASH}", device.getID()));
		}
        
        System.out.println("Narodmon temperature driver stopped!");
    }

    public void updated(Dictionary properties) throws ConfigurationException {
        synchronized(this) {
            System.out.println(properties == null);
            if(properties != null) {
            	url = (String) properties.get(URL);
            	int newDelay = (Integer) properties.get(SCHEDULED_DELAY);
            	if(newDelay != scheduledDelay) {
            		scheduledDelay = newDelay;
            		stopSheduled();
            		startSheduled();
            	}
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

    public String getTemplateObservation() {
        return templateObservation;
    }
    
    public String getUrl() {
		return url;
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
    
    private void readTemplates() {
        try {
            this.templateDescription = IOUtils.toString(DeviceDriverImpl.class
                    .getResourceAsStream("/ru/semiot/platform/drivers/narodmon/temperature/descriptionNarodmonTemperature.ttl"));
            this.templateObservation = IOUtils.toString(DeviceDriverImpl.class
                    .getResourceAsStream("/ru/semiot/platform/drivers/narodmon/temperature/observation.ttl"));
        } catch (IOException ex) {
            System.out.println("Can't read templates");
            throw new IllegalArgumentException(ex);
        }
    }
    
    public void startSheduled() {
		if (this.handle != null)
			stopSheduled();

		this.handle = this.scheduler.scheduleAtFixedRate(this.scheduledDevice,
				1, scheduledDelay, MINUTES);
		System.out.println("UScheduled started. Repeat will do every "
				+ String.valueOf(scheduledDelay) + " minutes");
	}

	public void stopSheduled() {
		if (handle == null)
			return;

		handle.cancel(true);
		handle = null;
		System.out.println("UScheduled stoped");
	}

}
