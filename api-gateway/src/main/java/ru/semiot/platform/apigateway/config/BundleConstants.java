package ru.semiot.platform.apigateway.config;

public class BundleConstants {
	
	private static String urlWebConsole = "http://deviceproxyservice:8181";
	
	public static String urlBundles = urlWebConsole + "/system/console/bundles";
	public static String urlBundlesJson = urlWebConsole + "/system/console/bundles.json";
	public static String urlDriversJson = "https://raw.githubusercontent.com/semiotproject/semiot-platform/bundles/drivers.json";
	public static String urlServicesJson = urlWebConsole + "/system/console/services.json";
	public static String urlConfigMgr = urlWebConsole + "/system/console/configMgr/";
	public static String urlStatusConfigurationsJson = urlWebConsole + "/system/console/status-Configurations.json";
	public static int countDefaultBundles = 14;
	
	public static String managerDomain = "ru.semiot.platform.deviceproxyservice.manager.domain";
	public static String managerApi = "ru.semiot.platform.deviceproxyservice.api.drivers.DeviceManager";
	
}
