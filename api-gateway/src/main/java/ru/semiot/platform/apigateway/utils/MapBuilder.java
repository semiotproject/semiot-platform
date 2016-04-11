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
        if (map.containsKey(key)) {
            Object o = map.get(key);
            if(o instanceof List) {
                List<Object> vs = (List<Object>) o;
                vs.addAll(Arrays.asList(values));
                map.put(key, vs);
            } else {
                List<Object> vs = new ArrayList<>();
                vs.add(o);
                vs.addAll(Arrays.asList(values));
                map.put(key, vs);
            }
        } else {
            if (values.length > 1) {
                map.put(key, Arrays.asList(values));
            }
            if (values.length == 1) {
                map.put(key, values[0]);
            }
        }

        return this;
    }

    public Map<String, Object> build() {
        return map;
    }

}
