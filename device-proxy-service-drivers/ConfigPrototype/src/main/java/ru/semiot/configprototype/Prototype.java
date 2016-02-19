package ru.semiot.configprototype;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
public class Prototype implements BundleActivator {

    private ServiceRegistration cService;
    private boolean flag = true;

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("Hello from Prototype.start!");
        Dictionary props = new Hashtable();
        props.put("service.pid", "ru.semiot.ConfigPrototype");
        cService = context.registerService(ManagedService.class.getName(),
                new Confi(), props);
        System.out.println("Props size " + props.size());
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Hello from Prototype.stop!");
        if (cService != null) {
            cService.unregister();
            cService = null;
        }
        System.out.println("________________________________________");
    }

    public void runMan() throws InterruptedException {
        int i = 0;
        while (true && i < 30) {
            System.out.println("" + ++i);
            Thread.sleep(1000);
        }
    }

}
