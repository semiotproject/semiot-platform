package ru.semiot.platform.drivers.simulator;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;

import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriver;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class Activator extends DependencyActivatorBase {

    public static final String SERVICE_PID = "ru.semiot.platform.drivers.simulator";
    
    @Override
    public void init(BundleContext bc, DependencyManager manager) throws Exception {
    	BasicConfigurator.configure();
    	Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
        
        manager.add(createComponent()
                .setInterface(DeviceDriver.class.getName(), null)
                .setImplementation(DeviceDriverImpl.class)
                .add(createServiceDependency()
                        .setService(DeviceManager.class)
                        .setRequired(true))
                .add(createConfigurationDependency().setPid(SERVICE_PID)));
    }

}
