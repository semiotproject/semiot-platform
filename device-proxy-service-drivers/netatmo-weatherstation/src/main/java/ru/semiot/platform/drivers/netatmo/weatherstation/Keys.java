package ru.semiot.platform.drivers.netatmo.weatherstation;

public abstract class Keys {

    public static final String DRIVER_PID
            = "ru.semiot.platform.drivers.netatmo-weatherstation";
    public static final String PREFIX = "ru.semiot";
    public static final String LONGITUDE_NORTH_EAST = PREFIX + ".longitude_ne";
    public static final String LATITUDE_NORTH_EAST = PREFIX + ".lattitude_ne";
    public static final String LONGITUDE_SOUTH_WEST = PREFIX + ".longitude_sw";
    public static final String LATITUDE_SOUTH_WEST = PREFIX + ".lattitude_sw";
    public static final String POLLING_INTERVAL = PREFIX + ".pollingInterval";
    public static final String POLLING_START_PAUSE = PREFIX + ".initialDelay";
    public static final String USERNAME = PREFIX + ".username";
    public static final String PASSWORD = PREFIX + ".password";
    public static final String CLIENT_APP_ID = PREFIX + ".clientAppID";
    public static final String CLIENT_SECRET = PREFIX + ".clientAppSecret";
    public static final String ONLY_NEW_OBS = PREFIX + ".onlyNewObservations";
    public static final String AREA = PREFIX + ".area";
}
