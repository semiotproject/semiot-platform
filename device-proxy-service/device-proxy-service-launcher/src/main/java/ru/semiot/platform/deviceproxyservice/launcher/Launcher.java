package ru.semiot.platform.deviceproxyservice.launcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    private static Framework framework = null;

    public static void main(String args[]) throws BundleException {

        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load(FrameworkFactory.class);
        FrameworkFactory frameworkFactory = loader.iterator().next();

        Map<String, String> config = new HashMap<>();
        config.put("org.osgi.service.http.port", "8181");

        Runtime.getRuntime().addShutdownHook(new Thread("Felix Shutdown Hook") {
            @Override
            public void run() {
                try {
                    if (framework != null) {
                        System.out.println("Framework stoping");
                        framework.stop();
                        framework.waitForStop(0);
                        System.out.println("Framework stoped");
                    }
                } catch (BundleException | InterruptedException ex) {
                    logger.error("Error stopping framework: ", ex);
                }
            }
        });

        try {
            framework = frameworkFactory.newFramework(config);
            // framework.init();
            framework.start();

            BundleContext context = framework.getBundleContext();

            List<Bundle> bundles = new ArrayList<>();

            //Apache Felix bundles
            bundles.add(context.installBundle(
                    "https://github.com/semiotproject/semiot-platform/blob/bundles/felix-bundles/org.apache.felix.configadmin-1.8.8.jar?raw=true"));
            bundles.add(context.installBundle(
                    "https://raw.githubusercontent.com/semiotproject/semiot-platform/bundles/felix-bundles/org.apache.felix.log-1.0.1.jar"));
            bundles.add(context.installBundle(
                    "https://github.com/semiotproject/semiot-platform/blob/bundles/felix-bundles/org.apache.felix.metatype-1.1.2.jar?raw=true"));
            bundles.add(context.installBundle(
                    "https://github.com/semiotproject/semiot-platform/blob/bundles/felix-bundles/org.osgi.compendium-1.4.0.jar?raw=true"));
            bundles.add(context.installBundle(
                    "https://github.com/semiotproject/semiot-platform/blob/bundles/felix-bundles/org.apache.felix.webconsole-4.2.12-all.jar?raw=true"));
            bundles.add(context.installBundle(
                    "https://raw.githubusercontent.com/semiotproject/semiot-platform/bundles/felix-bundles/org.apache.felix.http.servlet-api-1.1.2.jar"));
            bundles.add(context.installBundle(
                    "https://raw.githubusercontent.com/semiotproject/semiot-platform/bundles/felix-bundles/org.apache.felix.http.api-3.0.0.jar"));
            bundles.add(context.installBundle(
                    "https://github.com/semiotproject/semiot-platform/blob/bundles/felix-bundles/org.apache.felix.http.jetty-3.1.0.jar?raw=true"));
            bundles.add(context.installBundle(
                    "https://github.com/semiotproject/semiot-platform/blob/bundles/felix-bundles/org.apache.felix.eventadmin-1.4.4.jar?raw=true"));
            bundles.add(context.installBundle(
                    "https://github.com/semiotproject/semiot-platform/blob/bundles/felix-bundles/org.apache.felix.dependencymanager-4.1.0.jar?raw=true"));
            bundles.add(context.installBundle("http://central.maven.org/maven2/com/github/livesense/org.liveSense.fragment.sun.misc/1.0.5/org.liveSense.fragment.sun.misc-1.0.5.jar"));
            Bundle band = context.installBundle("file:C:\\Users\\Den\\Documents\\NetBeansProjects\\ConfigPrototype\\target\\ConfigPrototype-latest.jar");
            bundles.add(band);
            
            //Device Proxy Service bundles
            bundles.add(context.installBundle(
                    "device-proxy-service-api-1.0-SNAPSHOT.jar",
                    Launcher.class.getResourceAsStream("/bundles/device-proxy-service-api-1.0-SNAPSHOT.jar")));
            bundles.add(context.installBundle(
                    "device-proxy-service-manager-1.0-SNAPSHOT.jar",
                    Launcher.class.getResourceAsStream("/bundles/device-proxy-service-manager-1.0-SNAPSHOT.jar")));

            logger.info("Installed all bundles!");

            logger.info("Starting all of them...");
            for (Bundle bundle : bundles) {
                bundle.start();

                logger.info("Started [{}:{}]. Current state: {}",
                        bundle.getSymbolicName(), bundle.getVersion(), bundle.getState());
            }

            BundleContext cont = band.getBundleContext();
            ServiceReference caRef = cont.getServiceReference(ConfigurationAdmin.class.getName());
            
            if (caRef != null) {
                Object configAdmin =  cont.getService(caRef);
                Configuration configur = ((ConfigurationAdmin)configAdmin).getConfiguration("ru.semiot.ConigPrototype");

                Dictionary props = configur.getProperties();
                if (props == null) {
                    props = new Hashtable();
                }
                // configure the Dictionary
                props.put("Hello","World");

                //push the configuration dictionary to the SmsService
                configur.update(props);
            }
            else{
                System.out.println("Bad-bad-bad");
            }

            framework.waitForStop(0);
            System.exit(0);
        } catch (BundleException | InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            System.exit(0);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            System.exit(0);
        }
    }

}
