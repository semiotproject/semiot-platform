package ru.semiot.platform.drivers.winghouse.machinetool;

import java.util.Properties;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;

import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class Activator extends DependencyActivatorBase {

    public static final String PID = "ru.semiot.platform.drivers.winghouse.machinetool";

    @Override
    public void init(BundleContext bc, DependencyManager manager) throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Constants.SERVICE_PID, PID);
        
        manager.add(createComponent()
                .setInterface(new String[] {DeviceDriver.class.getName(), 
                		ManagedService.class.getName()}, properties)
                .setImplementation(DeviceDriverImpl.class)
                .add(createServiceDependency()
                        .setService(DeviceManager.class)
                        .setRequired(true)));
    }

}
