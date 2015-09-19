package ru.semiot.platform.deviceproxyservice.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.Bundle;

public class Launcher {

    public static void main(String args[]) throws BundleException {

        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load(FrameworkFactory.class);
        FrameworkFactory frameworkFactory = loader.iterator().next();

        Map<String, String> config = new HashMap<>();
        config.put("org.osgi.service.http.port", "8181");

        Framework framework = frameworkFactory.newFramework(config);
        framework.start();

        BundleContext context = framework.getBundleContext();

        List<Bundle> bundles = new ArrayList<>();

        //Apache Felix bundles
        bundles.add(context.installBundle(
                "http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.configadmin-1.8.8.jar"));
        bundles.add(context.installBundle(
                "http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.log-1.0.1.jar"));
        bundles.add(context.installBundle(
                "http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.metatype-1.1.2.jar"));
        bundles.add(context.installBundle(
                "http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.webconsole-4.2.10-all.jar"));
        bundles.add(context.installBundle(
                "http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.http.servlet-api-1.1.2.jar"));
        bundles.add(context.installBundle(
                "http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.http.api-3.0.0.jar"));
        bundles.add(context.installBundle(
                "http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.http.jetty-3.1.0.jar"));
        bundles.add(context.installBundle(
                "http://apache-mirror.rbc.ru/pub/apache//felix/org.apache.felix.eventadmin-1.4.4.jar"));
        bundles.add(context.installBundle(
                "http://central.maven.org/maven2/org/apache/felix/org.apache.felix.dependencymanager/4.1.0/org.apache.felix.dependencymanager-4.1.0.jar"));
                
        //Device Proxy Service bundles
        bundles.add(context.installBundle(
                "device-proxy-service-api-1.0-SNAPSHOT.jar", 
                Launcher.class.getResourceAsStream("/bundles/device-proxy-service-api-1.0-SNAPSHOT.jar")));
        bundles.add(context.installBundle(
                "device-proxy-service-manager-1.0-SNAPSHOT.jar", 
                Launcher.class.getResourceAsStream("/bundles/device-proxy-service-manager-1.0-SNAPSHOT.jar")));
        
        System.out.println("Installed all bundles!");
        
        System.out.println("Starting all of them...");
        for (Bundle bundle : bundles) {
            bundle.start();
            
            System.out.format("Started [%s:%s]. Current state: %s\n", 
                    bundle.getSymbolicName(), bundle.getVersion(), bundle.getState());
        }
    }

}
