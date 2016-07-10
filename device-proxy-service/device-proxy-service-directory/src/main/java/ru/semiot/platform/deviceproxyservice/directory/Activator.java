package ru.semiot.platform.deviceproxyservice.directory;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;
import ru.semiot.platform.deviceproxyservice.api.manager.DirectoryService;

import java.util.Properties;

public class Activator extends DependencyActivatorBase {

  private static final String PID = "ru.semiot.platform.deviceproxyservice.directory";

  @Override
  public void init(BundleContext bc, DependencyManager manager)
      throws Exception {
    Properties properties = new Properties();
    properties.setProperty(Constants.SERVICE_PID, PID);

    manager.add(createComponent()
        .setInterface(new String[]{
            DirectoryService.class.getName(),
            ManagedService.class.getName()
        }, properties)
        .setImplementation(DirectoryServiceImpl.class)
        .add(createConfigurationDependency().setPid(PID)));
  }
}
