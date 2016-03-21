package ru.semiot.platform.drivers.netatmo.weatherstation;

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
    private static final String LOCATION_KEY = "location";
    private static final String PLACE_KEY = "place";
    private static final String ID_KEY = "_id";
    private static final String MEASURES_KEY = "measures";
    private static final String TYPE_KEY = "type";
    private static final String RES_KEY = "res";
    private static final String ALTITUDE_KEY = "altitude";

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

    private boolean isMeasuresTemperatureAndHumidity(JSONObject device)
            throws JSONException {
        JSONObject obj = device.getJSONObject(MEASURES_KEY);

        Iterator keys = obj.keys();

        while (keys.hasNext()) {
            String sensorId = (String) keys.next();
            JSONArray types = obj.getJSONObject(sensorId).optJSONArray(TYPE_KEY);
            if (types!=null && types.length() == 2) {
                if (JSONUtils.contains(types, WeatherStationObservation.TEMPERATURE_TYPE)
                        && JSONUtils.contains(types, WeatherStationObservation.HUMIDITY_TYPE)) {
                    return true;
                }
            }
        }

        return false;
    }

    private WeatherStationObservation findObservation(JSONObject device, String deviceId, String type)
            throws JSONException {
        JSONObject obj = device.getJSONObject(MEASURES_KEY);

        Iterator keys = obj.keys();

        while (keys.hasNext()) {
            String sensorId = (String) keys.next();
            JSONArray types = obj.getJSONObject(sensorId).optJSONArray(TYPE_KEY);
            if (types != null) {
                int index = JSONUtils.find(types, type);
                if (index > -1) {
                    String timestamp = (String) obj.getJSONObject(sensorId)
                            .getJSONObject(RES_KEY).keys().next();
                    String value = String.valueOf(obj.getJSONObject(sensorId)
                            .getJSONObject(RES_KEY).getJSONArray(timestamp)
                            .getDouble(index));

                    return new WeatherStationObservation(
                            deviceId, timestamp, value, type);
                }
            } else {
                return null;
            }
        }

        return null;
    }

    public List<WeatherStation> parseStations(JSONArray devices) {
        List<WeatherStation> stations = new ArrayList<>();

        for (int i = 0; i < devices.length(); i++) {
            try {
                JSONObject device = devices.getJSONObject(i);

                String id = hash(driverName, device.getString(ID_KEY));
                JSONObject place = device.getJSONObject(PLACE_KEY);
                JSONArray location = place
                        .getJSONArray(LOCATION_KEY);

                String longitude = String.valueOf(location.getDouble(0));
                String latitude = String.valueOf(location.getDouble(1));
                String altitude = String.valueOf(place.getDouble(ALTITUDE_KEY));

                if (isMeasuresTemperatureAndHumidity(device)) {
                    //We want stations measuring temperature
                    WeatherStation station = new WeatherStation(
                            id, latitude, longitude, altitude);

                    stations.add(station);
                }
            } catch (JSONException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        return stations;
    }

    public List<WeatherStationObservation> parseObservations(JSONArray devices) {
        List<WeatherStationObservation> observations = new ArrayList<>();

        for (int i = 0; i < devices.length(); i++) {
            try {
                JSONObject device = devices.getJSONObject(i);

                String id = hash(driverName, device.getString(ID_KEY));

                WeatherStationObservation temperature = findObservation(
                        device, id, WeatherStationObservation.TEMPERATURE_TYPE);

                WeatherStationObservation humidity = findObservation(
                        device, id, WeatherStationObservation.HUMIDITY_TYPE);

                if (temperature != null && humidity != null) {
                    observations.add(temperature);
                    observations.add(humidity);
                }

            } catch (JSONException ex) {
                logger.warn(ex.getMessage(), ex);
            }
        }

        return observations;
    }

}
