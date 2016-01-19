package ru.semiot.platform.drivers.netatmo.weatherstation;

public abstract class Keys {

    public static final String DRIVER_PID
            = "ru.semiot.platform.drivers.netatmo.weatherstation";
    public static final String LONGITUDE_NORTH_EAST = DRIVER_PID + ".longitude_ne";
    public static final String LATITUDE_NORTH_EAST = DRIVER_PID + ".lattitude_ne";
    public static final String LONGITUDE_SOUTH_WEST = DRIVER_PID + ".longitude_sw";
    public static final String LATITUDE_SOUTH_WEST = DRIVER_PID + ".lattitude_sw";
    public static final String POLLING_INTERVAL = DRIVER_PID + ".scheduled_delay";
    public static final String POLLING_START_PAUSE = DRIVER_PID + ".start_pause";
    public static final String USERNAME = DRIVER_PID + ".username";
    public static final String PASSWORD = DRIVER_PID + ".pass";
    public static final String CLIENT_APP_ID = DRIVER_PID + ".client_app_id";
    public static final String CLIENT_SECRET = DRIVER_PID + ".client_secret";
}
