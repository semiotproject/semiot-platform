package ru.semiot.platform.drivers.netatmo.temperature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeatherStationFactory {

    private static final Logger logger = LoggerFactory.getLogger(WeatherStationFactory.class);

    private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;
    private static final String TEMPERATURE_KEY = "temperature";
    private static final String LOCATION_KEY = "location";
    private static final String PLACE_KEY = "place";
    private static final String ID_KEY = "_id";
    private static final String MEASURES_KEY = "measures";
    private static final String TYPE_KEY = "type";
    private static final String RES_KEY = "res";

    private final String driverName;

    public WeatherStationFactory(String driverName) {
        this.driverName = driverName;
    }

    public String hash(String prefix, String id) {
        String name = prefix + id;
        int h = FNV_32_INIT;
        final int len = name.length();
        for (int i = 0; i < len; i++) {
            h ^= name.charAt(i);
            h *= FNV_32_PRIME;
        }
        long longHash = h & 0xffffffffl;
        return String.valueOf(longHash);
    }

    private WeatherStation.Observation findObservation(JSONObject device, String name)
            throws JSONException {
        JSONObject obj = device.getJSONObject(MEASURES_KEY);

        Iterator keys = obj.keys();

        while (keys.hasNext()) {
            String sensorId = (String) keys.next();
            JSONArray types = obj.getJSONObject(sensorId).optJSONArray(TYPE_KEY);
            if (types != null) {
                int index = JSONUtils.find(types, name);
                if (index > -1) {
                    String timestamp = (String) obj.getJSONObject(sensorId)
                            .getJSONObject(RES_KEY).keys().next();
                    double value = obj.getJSONObject(sensorId)
                            .getJSONObject(RES_KEY).getJSONArray(timestamp)
                            .getDouble(index);

                    return new WeatherStation.Observation(timestamp, value);
                }
            } else {
                return null;
            }
        }

        return null;
    }

    public List<WeatherStation> parse(JSONArray devices) {
        List<WeatherStation> stations = new ArrayList<>();

        for (int i = 0; i < devices.length(); i++) {
            try {
                JSONObject device = devices.getJSONObject(i);

                String id = hash(driverName, device.getString(ID_KEY));
                JSONArray location = device.getJSONObject(PLACE_KEY)
                        .getJSONArray(LOCATION_KEY);

                double longitude = location.getDouble(0);
                double latitude = location.getDouble(1);

                WeatherStation.Observation temperature = findObservation(
                        device, TEMPERATURE_KEY);

                if (temperature != null) {
                    //We want stations measuring temperature
                    WeatherStation station = new WeatherStation(
                            id, latitude, longitude);

                    station.setTemperature(temperature);

                    stations.add(station);
                }
            } catch (JSONException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        return stations;
    }

}
