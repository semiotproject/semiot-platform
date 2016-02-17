package ru.semiot.platform.apigateway.config;

import org.aeonbits.owner.ConfigFactory;
import ru.semiot.platform.apigateway.ServerConfig;

public class BundleConstants {
    
    private static final ServerConfig CONFIG = ConfigFactory.create(ServerConfig.class);

    public static final String urlBundles = CONFIG.consoleEndpoint() + "/system/console/bundles";
    public static final String urlBundlesJson = CONFIG.consoleEndpoint() + "/system/console/bundles.json";
    public static final String urlDriversJson = CONFIG.repositoryEndpoint();
    public static final String urlServicesJson = CONFIG.consoleEndpoint() + "/system/console/services.json";
    public static final String urlConfigMgr = CONFIG.consoleEndpoint() + "/system/console/configMgr/";
    public static final String urlStatusConfigurationsJson = 
            CONFIG.consoleEndpoint() + "/system/console/status-Configurations.json";
    public static final int countDefaultBundles = 14;

    public static final String managerDomain = "ru.semiot.platform.domain";
    public static final String managerApi = "ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager";

}
