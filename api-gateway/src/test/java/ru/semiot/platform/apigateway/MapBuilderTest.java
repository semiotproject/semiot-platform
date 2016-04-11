package ru.semiot.platform.apigateway;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import ru.semiot.platform.apigateway.utils.JsonLdKeys;
import ru.semiot.platform.apigateway.utils.MapBuilder;

public class MapBuilderTest {
    

    @Test
    public void testPut1() {
        Map<String, Object> actual = MapBuilder.newMap()
                .put(JsonLdKeys.ID, "<1>")
                .put(JsonLdKeys.TYPE, "h:a")
                .put(JsonLdKeys.TYPE, "h:b")
                .build();
        
        Map<String, Object> expected = new HashMap<>();
        expected.put(JsonLdKeys.ID, "<1>");
        List<Object> types = new ArrayList<>();
        types.add("h:a");
        types.add("h:b");
        expected.put(JsonLdKeys.TYPE, types);
        
        assertEquals(expected, actual);
    }
}
