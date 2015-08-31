package ru.semiot.platform.deviceproxyservice.impl.devicemanager;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager;

public class Activator extends DependencyActivatorBase {

    @Override
    public void init(BundleContext bc, DependencyManager manager) throws Exception {
        manager.add(createComponent()
                .setInterface(DeviceManager.class.getName(), null)
                .setImplementation(DeviceManagerImpl.class));
    }

}
