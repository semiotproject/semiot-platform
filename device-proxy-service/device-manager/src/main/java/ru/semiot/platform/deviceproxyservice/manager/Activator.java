package ru.semiot.platform.deviceproxyservice.manager;

import java.util.Hashtable;
import java.util.Properties;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;

import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class Activator extends DependencyActivatorBase {

    private static final String PID = "ru.semiot.platform.deviceproxyservice.manager";

    @Override
    public void init(BundleContext bc, DependencyManager manager) throws Exception {
    	BasicConfigurator.configure();
    	Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
    	
        Properties properties = new Properties();
        properties.setProperty(Constants.SERVICE_PID, PID);
        
        manager.add(createComponent()
                .setInterface(new String[] {DeviceManager.class.getName(), 
                		ManagedService.class.getName()}, properties)
                .setImplementation(DeviceManagerImpl.class)
                .add(createConfigurationDependency().setPid(PID)));
    }
    
}
