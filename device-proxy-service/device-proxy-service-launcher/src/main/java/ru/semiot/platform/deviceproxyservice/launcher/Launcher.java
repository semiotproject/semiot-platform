package ru.semiot.platform.deviceproxyservice.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
    private static final String BUNDLES_PATH = "/bundles/";
    private static final String BUNDLE_NAME_PREFIX = "file://" + BUNDLES_PATH;
    private static final String[] LOCAL_BUNDLES = new String[]{
            //Apache Felix bundles and their dependencies
            "org.apache.felix.configadmin-1.8.8.jar",
            "org.apache.felix.log-1.0.1.jar",
            "org.apache.felix.metatype-1.1.2.jar",
            "org.osgi.compendium-1.4.0.jar",
            "org.apache.felix.webconsole-4.2.12-all.jar",
            "org.apache.felix.http.servlet-api-1.1.2.jar",
            "org.apache.felix.http.api-3.0.0.jar",
            "org.apache.felix.http.jetty-3.1.0.jar",
            "org.apache.felix.eventadmin-1.4.4.jar",
            "org.apache.felix.dependencymanager-4.1.0.jar",
            "org.liveSense.fragment.sun.misc-1.0.5.jar",
            "org.apache.servicemix.bundles.json-20140107_1.jar",
            "jersey-all-2.22.2.jar",
            "org.apache.felix.http.whiteboard-3.0.0.jar",
            "publisher-5.3.1.jar",

            //Device Proxy Service bundles
            "device-proxy-service-api-1.0-SNAPSHOT.jar",
            "device-proxy-service-manager-1.0-SNAPSHOT.jar",
            "rs-proxy-service-1.0.0-SNAPSHOT.jar",
            "configurator-1.0.0.jar",

            //Device Proxy Service dependencies
            "slf4j-api-1.7.21.jar",
                "logback-core-1.1.7.jar",
                "logback-classic-1.1.7.jar",
            "jena-osgi-3.0.0.jar",
                "jackson-core-2.6.6.jar",
                "jackson-databind-2.6.6.jar",
                "jackson-annotations-2.6.6.jar",
                "jsonld-java-0.8.2.jar",
                "httpcore-osgi-4.4.4.jar",
                "httpclient-osgi-4.5.1.jar",
                "jcl-over-slf4j-1.7.21.jar",
                "commons-cli-1.3.1.jar",
                "commons-csv-1.2.jar",
                "commons-lang3-3.4.jar",
                "log4j-over-slf4j-1.7.21.jar",
                "libthrift-0.9.3.jar"
    };

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
                        System.out.println("Framework stopping");
                        framework.stop();
                        framework.waitForStop(0);
                        System.out.println("Framework stopped");
                    }
                } catch (BundleException | InterruptedException ex) {
                    logger.error("Error stopping framework: ", ex);
                }
            }
        });

        try {
            framework = frameworkFactory.newFramework(config);
            framework.start();

            BundleContext context = framework.getBundleContext();

            List<Bundle> bundles = new ArrayList<>();

            for(String bundleName : LOCAL_BUNDLES) {
                bundles.add(loadAndInstallBundle(context, bundleName));
            }
            logger.info("Loaded and installed all local bundles!");

            logger.info("Starting all of them...");
            for (Bundle bundle : bundles) {
                bundle.start();

                logger.info("Started [{}:{}]. Current state: {}",
                        bundle.getSymbolicName(), bundle.getVersion(), bundle.getState());
            }
            logger.info("All bundles started!");
            framework.waitForStop(0);
            System.exit(0);
        } catch (BundleException | InterruptedException ex) {
            logger.error(ex.getMessage(), ex);
            System.exit(0);
        }
    }

    private static Bundle loadAndInstallBundle(BundleContext context, String name)
            throws BundleException {
        return context.installBundle(BUNDLE_NAME_PREFIX + name,
                Launcher.class.getResourceAsStream(BUNDLES_PATH + name));
    }

}
