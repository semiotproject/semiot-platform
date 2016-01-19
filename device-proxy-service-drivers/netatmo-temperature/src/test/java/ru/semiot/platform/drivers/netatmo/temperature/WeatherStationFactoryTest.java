package ru.semiot.platform.drivers.netatmo.temperature;

import ru.semiot.platform.drivers.netatmo.weatherstation.WeatherStationObservation;
import ru.semiot.platform.drivers.netatmo.weatherstation.WeatherStation;
import ru.semiot.platform.drivers.netatmo.weatherstation.WeatherStationFactory;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Test;
import static org.junit.Assert.*;

public class WeatherStationFactoryTest {

    private static final String JSON_DEVICES = "[{\"measures\":{\"70:ee:50:06:0f:12\":{"
            + "\"res\":{\"1452115810\":[1007.2]},\"type\":[\"pressure\"]},"
            + "\"02:00:00:05:ee:bc\":{\"res\":{\"1452115809\":[-18.4,71]},"
            + "\"type\":[\"temperature\",\"humidity\"]}},\"_id\":\"70:ee:50:06:0f:12\","
            + "\"place\":{\"altitude\":15,\"timezone\":\"Europe/Moscow\",\"location\":["
            + "30.298187,60.021748]},\"mark\":15,\"modules\":[\"02:00:00:05:ee:bc\"]},"
            + "{\"measures\":{\"70:ee:50:03:df:36\":{\"res\":{\"1452115810\":[1012.7]},"
            + "\"type\":[\"pressure\"]},\"02:00:00:03:bd:9a\":{\"res\":{\"1452115790\":"
            + "[-18.9,68]},\"type\":[\"temperature\",\"humidity\"]}},\"_id\":\"70:ee:50:03:df:36\","
            + "\"place\":{\"altitude\":24,\"timezone\":\"Europe/Moscow\",\"location\":"
            + "[30.29713856,60.0255383]},\"mark\":0,\"modules\":[\"02:00:00:03:bd:9a\"]}]";
    private static final String JSON_DEVICES_EMPTY = "[]";
    private static final String DRIVER_PID = "ru.semiot.platform.drivers.netatmo";

    @Test
    public void testParseJSONArray() throws JSONException {
        WeatherStationFactory wsFactory = new WeatherStationFactory(DRIVER_PID);

        List<WeatherStation> actual = wsFactory.parseStations(new JSONArray(JSON_DEVICES));

        List<WeatherStation> expected = new ArrayList<>();
        WeatherStation one = new WeatherStation(wsFactory.hash(DRIVER_PID, "70:ee:50:06:0f:12"),
                "60.021748", "30.298187", "15.0");
        WeatherStation two = new WeatherStation(wsFactory.hash(DRIVER_PID, "70:ee:50:03:df:36"),
                "60.0255383", "30.29713856", "24.0");
        expected.add(one);
        expected.add(two);

        assertArrayEquals(expected.toArray(), actual.toArray());
    }

    @Test
    public void testConvertEmptyDeviceArray() throws JSONException {
        WeatherStationFactory wsFactory = new WeatherStationFactory(DRIVER_PID);

        List<WeatherStation> actual = wsFactory.parseStations(new JSONArray(JSON_DEVICES_EMPTY));

        List<WeatherStation> expected = new ArrayList<>();

        assertArrayEquals(actual.toArray(), expected.toArray());
    }

    @Test
    public void testParseObservations() throws JSONException {

        WeatherStationFactory wsFactory = new WeatherStationFactory(DRIVER_PID);

        List<WeatherStationObservation> actual = wsFactory.parseObservations(
                new JSONArray(JSON_DEVICES));

        List<WeatherStationObservation> expected = new ArrayList<>();
        WeatherStationObservation one = new WeatherStationObservation(
                wsFactory.hash(DRIVER_PID, "70:ee:50:06:0f:12"),
                "1452115809", "-18.4", WeatherStationObservation.TEMPERATURE_TYPE);
        WeatherStationObservation two = new WeatherStationObservation(
                wsFactory.hash(DRIVER_PID, "70:ee:50:06:0f:12"),
                "1452115809", "71.0", WeatherStationObservation.HUMIDITY_TYPE);
        WeatherStationObservation three = new WeatherStationObservation(
                wsFactory.hash(DRIVER_PID, "70:ee:50:03:df:36"),
                "1452115790", "-18.9", WeatherStationObservation.TEMPERATURE_TYPE);
        WeatherStationObservation four = new WeatherStationObservation(
                wsFactory.hash(DRIVER_PID, "70:ee:50:03:df:36"),
                "1452115790", "68.0", WeatherStationObservation.HUMIDITY_TYPE);
        
        expected.add(one);
        expected.add(two);
        expected.add(three);
        expected.add(four);

        assertArrayEquals(actual.toArray(), expected.toArray());
    }

}
