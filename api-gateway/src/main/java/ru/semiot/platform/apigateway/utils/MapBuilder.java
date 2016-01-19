package ru.semiot.platform.apigateway.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapBuilder {

    private final Map<String, Object> map;

    private MapBuilder() {
        this.map = new HashMap<>();
    }

    public static MapBuilder newMap() {
        return new MapBuilder();
    }

    public MapBuilder put(String key, Object... values) {
        if (values.length > 1) {
            List<Object> value = Arrays.asList(values);
            map.put(key, value);
        } if (values.length == 1) {
            map.put(key, values[0]);
        }
        
        return this;
    }

    public Map<String, Object> build() {
        return map;
    }

}
