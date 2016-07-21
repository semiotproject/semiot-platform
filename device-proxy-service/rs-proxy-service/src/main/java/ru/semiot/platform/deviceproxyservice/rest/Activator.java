package ru.semiot.platform.deviceproxyservice.rest;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceDriverManager;

import java.util.Properties;


public class Activator extends DependencyActivatorBase {

  @Override
  public void init(BundleContext bc, DependencyManager manager) throws Exception {
    //RemoveServiceImpl
    manager.add(createComponent()
        .setInterface(Object.class.getName(), new Properties())
        .setImplementation(RemoveServiceImpl.class)
        .add(createServiceDependency()
            .setService(DeviceDriverManager.class)
            .setRequired(true)));

    //StatusResource
    manager.add(createComponent()
        .setInterface(Object.class.getName(), new Properties())
        .setImplementation(StatusResource.class));

    //CommandAPI
    manager.add(createComponent()
        .setInterface(Object.class.getName(), new Properties())
        .setImplementation(CommandAPI.class)
        .add(createServiceDependency()
            .setService(DeviceDriverManager.class)
            .setRequired(true)));
  }

}
