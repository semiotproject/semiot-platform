package ru.semiot.platform.deviceproxyservice.manager;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;

import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class Activator extends DependencyActivatorBase {

    private static final String SERVICE_PID = "ru.semiot.platform.deviceproxyservice.manager";

    @Override
    public void init(BundleContext bc, DependencyManager manager) throws Exception {
    	BasicConfigurator.configure();
    	Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
    	
        manager.add(createComponent()
                .setInterface(DeviceManager.class.getName(), null)
                .setImplementation(DeviceManagerImpl.class)
                .add(createConfigurationDependency().setPid(SERVICE_PID)));
    }
    
}
