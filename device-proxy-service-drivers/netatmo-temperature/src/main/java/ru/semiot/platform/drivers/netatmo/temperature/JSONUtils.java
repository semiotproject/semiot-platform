package ru.semiot.platform.drivers.netatmo.temperature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {

    public static int find(JSONArray array, String element) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            if (array.getString(i).equals(element)) {
                return i;
            }
        }

        return -1;
    }

}
