package ru.semiot.platform.drivers.netatmo.temperature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.MINUTES;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import ru.semiot.platform.deviceproxyservice.api.drivers.Device;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class DeviceDriverImpl implements DeviceDriver, ManagedService {

    private static final Logger logger = Logger.getLogger(DeviceDriverImpl.class);
    private static final String SCHEDULED_DELAY = Activator.PID + ".scheduled_delay";
    private static final String LAT_NE = Activator.PID + ".lattitude_ne";
    private static final String LON_NE = Activator.PID + ".longitude_ne";
    private static final String LAT_SW = Activator.PID + ".lattitude_sw";
    private static final String LON_SW = Activator.PID + ".longitude_sw";

    private final List<Device> listDevices = Collections.synchronizedList(new ArrayList<Device>());

    public static final String templateOffState = "prefix saref: <http://ontology.tno.nl/saref#> "
            + "<http://${DOMAIN}/${SYSTEM_PATH}/${DEVICE_HASH}> saref:hasState saref:OffState.";

    private ScheduledExecutorService scheduler;
    private ScheduledDevice scheduledDevice;
    private ScheduledFuture handle = null;
    private final String driverName = "Netatmo temperature";

    private volatile DeviceManager deviceManager;

    private String templateDescription;
    private String templateObservation;

    private int scheduledDelay = 30;
    private double lat_ne = 55.907042;
    private double lon_ne = 37.842851;
    private double lat_sw = 55.593313;
    private double lon_sw = 37.412678;

    public List<Device> listDevices() {
        return listDevices;
    }

    public void start() {
        logger.info("Netatmo temperature driver started!");

        readTemplates();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.scheduledDevice = new ScheduledDevice(this);
        startSheduled();
    }

    public void stop() {
        // перевод всех устройств в статус офф
        stopSheduled();

        for (Device device : listDevices) {
            System.out.println(templateOffState.replace("${DOMAIN}", getDomain())
                    .replace("${SYSTEM_PATH}", getPathSystemUri())
                    .replace("${DEVICE_HASH}", device.getID()));
            inactiveDevice(templateOffState.replace("${DOMAIN}", getDomain())
                    .replace("${SYSTEM_PATH}", getPathSystemUri())
                    .replace("${DEVICE_HASH}", device.getID()));
        }

        logger.info("Netatmo temperature driver stopped!");
    }
    
    public void updated(Dictionary properties) throws ConfigurationException {
        logger.info("Netatmo temperature driver updated");
        synchronized (this) {
            logger.debug("properties is " + (properties == null ? "null" : "not null"));
            if (properties != null) {
                lat_ne = (Double) properties.get(LAT_NE);
                lon_ne = (Double) properties.get(LON_NE);
                lat_sw = (Double) properties.get(LAT_SW);
                lon_sw = (Double) properties.get(LON_SW);
                int newDelay = (Integer) properties.get(SCHEDULED_DELAY);
                logger.debug("Get all parameters! Lat_ne=" + lat_ne + ", lat_sw=" + lat_sw + ", lon_ne=" + lon_ne + ", lon_sw=" + lon_sw + ", delay=" + newDelay);
                logger.debug("Try to start (" + (newDelay != scheduledDelay) + ")");
                if (newDelay != scheduledDelay) {
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

    public double getLat_ne() {
        return lat_ne;
    }

    public double getLon_ne() {
        return lon_ne;
    }

    public double getLat_sw() {
        return lat_sw;
    }

    public double getLon_sw() {
        return lon_sw;
    }

    private void readTemplates() {
        try {
            this.templateDescription = IOUtils.toString(DeviceDriverImpl.class
                    .getResourceAsStream("/ru/semiot/platform/drivers/netatmo/temperature/descriptionNetatmoTemperature.ttl"));
            this.templateObservation = IOUtils.toString(DeviceDriverImpl.class
                    .getResourceAsStream("/ru/semiot/platform/drivers/netatmo/temperature/observation.ttl"));
        } catch (IOException ex) {
            logger.error("Can't read templates");
            throw new IllegalArgumentException(ex);
        }
    }

    public void startSheduled() {
        logger.debug("Hello from startScheduled!");

        if (this.handle != null) {
            logger.debug("Try to stop scheduler");
            stopSheduled();
        }
        this.handle = this.scheduler.scheduleAtFixedRate(this.scheduledDevice,
                1, scheduledDelay, MINUTES);
        logger.debug("UScheduled started. Repeat will do every "
                + String.valueOf(scheduledDelay) + " minutes");
    }

    public void stopSheduled() {
        logger.debug("Hello from stopScheduled!");
        if (handle == null) {
            return;
        }

        handle.cancel(true);
        handle = null;
        logger.debug("UScheduled stoped");
    }

}
