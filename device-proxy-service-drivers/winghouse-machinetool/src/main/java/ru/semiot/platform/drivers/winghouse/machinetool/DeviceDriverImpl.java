package ru.semiot.platform.drivers.winghouse.machinetool;

import static java.util.concurrent.TimeUnit.SECONDS;
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

import org.apache.commons.io.IOUtils;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class DeviceDriverImpl implements DeviceDriver, ManagedService {

    private static final String UDP_PORT_KEY = Activator.PID + ".udp_port";
            
    private final List<Device> listDevices = Collections.synchronizedList(new ArrayList<Device>());
    private Map<String, MachineToolValue> oldValueMachineTools = 
			Collections.synchronizedMap(new HashMap<String, MachineToolValue>());
    private static final String templateOffState = "prefix saref: <http://ontology.tno.nl/saref#> "
			+ "<http://${DOMAIN}/${SYSTEM_PATH}/${DEVICE_HASH}> saref:hasState saref:OffState.";
    
    private ScheduledExecutorService scheduler;
	private ScheduledDeviceStatus scheduledDeviceStatus;
	private ScheduledFuture handle = null;
	private final String driverName = "Winghouse-machinetool";
    
    private volatile DeviceManager deviceManager;

    private String templateDescription;
    private String templateObservation;
    private EventLoopGroup group;
    private Channel channel;
    private Bootstrap bootstrap;
    private int port = 9500;

    public List<Device> listDevices() {
        return listDevices;
    }

    public void start() {
        System.out.println("Winghouse machine-tools driver started!");

        readTemplates();
        
        group = new NioEventLoopGroup();
        this.scheduler = Executors.newScheduledThreadPool(1);
		this.scheduledDeviceStatus = new ScheduledDeviceStatus();
		startSheduled();
        try {
        	bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new DeviceHandler(this));

            channel = bootstrap.bind(port).channel();
        } catch (Exception ex) {
            ex.printStackTrace();

            group.shutdownGracefully();
        }
    }

    public void stop() {
        if (channel != null) {
            channel.close();
        }
        if (group != null) {
        	try {
                group.shutdownGracefully();
                group.awaitTermination(15, TimeUnit.SECONDS);   	
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}
        }
        // перевод всех устройств в статус офф
        stopSheduled();
        for(Map.Entry<String, MachineToolValue> entry : oldValueMachineTools.entrySet()) {
        	entry.getValue().setTurnOn(false);
        }
        for (Device device : listDevices)
		{
        	System.out.println(templateOffState.replace("${DOMAIN}", getDomain())
        			.replace("${SYSTEM_PATH}", getPathSystemUri())
        			.replace("${DEVICE_HASH}", device.getID()));
        	inactiveDevice(templateOffState.replace("${DOMAIN}", getDomain())
        			.replace("${SYSTEM_PATH}", getPathSystemUri())
        			.replace("${DEVICE_HASH}", device.getID()));
		}
        
        System.out.println("Winghouse machine-tools driver stopped!");
    }

    public void updated(Dictionary properties) throws ConfigurationException {
        synchronized(this) {
            System.out.println(properties == null);
            if(properties != null) {
            	int newPort = (Integer) properties.get(UDP_PORT_KEY);
            	if(newPort != port) {
	            	channel.close();
	            	port = newPort;
	            	channel = bootstrap.bind(port).channel();
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
    
    public int getPort() {
		return port;
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

    public Map<String, MachineToolValue> getOldValueMachineTools() {
    	return oldValueMachineTools;
    }
    
    private void readTemplates() {
        try {
            this.templateDescription = IOUtils.toString(DeviceHandler.class
                    .getResourceAsStream("/ru/semiot/platform/drivers/winghouse/machinetool/descriptionMachineTools.ttl"));
            this.templateObservation = IOUtils.toString(DeviceHandler.class
                    .getResourceAsStream("/ru/semiot/platform/drivers/winghouse/machinetool/observation.ttl"));
        } catch (IOException ex) {
            System.out.println("Can't read templates");
            throw new IllegalArgumentException(ex);
        }
    }
    
    public void startSheduled() {
		if (this.handle != null)
			stopSheduled();

		int nDelay = 30;
		this.handle = this.scheduler.scheduleAtFixedRate(this.scheduledDeviceStatus,
				40, nDelay, SECONDS);
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
    
    private class ScheduledDeviceStatus implements Runnable {
		public void run() {
			long currentTimestamp = System.currentTimeMillis();
			for (Map.Entry<String, MachineToolValue> entry : oldValueMachineTools.entrySet())
			{
				if(entry.getValue().getTurnOn() == true &&
						entry.getValue().getTimestemp() + 30000 < currentTimestamp ) {
					inactiveDevice(templateOffState.replace("${DOMAIN}", getDomain())
							.replace("${SYSTEM_PATH}", getPathSystemUri())
		        			.replace("${DEVICE_HASH}", entry.getKey()));
					entry.getValue().setTurnOn(false);
					System.out.println(entry.getKey() + " saref:OffState" );
				}
			}
		}
	}

}
