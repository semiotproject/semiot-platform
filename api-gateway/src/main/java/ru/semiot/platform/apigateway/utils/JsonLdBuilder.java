package ru.semiot.platform.apigateway.utils;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonLdBuilder {
    
    private final Map<String, Object> context;
    private final Map<String, Object> content;
    
    public JsonLdBuilder() {
        this(new HashMap<>(), new HashMap<>());
    }
    
    public JsonLdBuilder(Map<String, Object> context) {
        this(context, new HashMap<>());
    }

    public JsonLdBuilder(
            Map<String, Object> context, Map<String, Object> content) {
        this.context = context;
        this.content = content;
    }
    
    public JsonLdBuilder add(String key, String value) {
        content.put(key, value);
        
        return this;
    }
    
    public JsonLdBuilder array(String key) {
        content.put(key, new ArrayList<>());
        
        return this;
    }
    
    public JsonLdBuilder arrayAdd(String key, Map<String, Object> value) {
        final Object o = content.get(key);
        if(o != null && o instanceof List) {
            final List lo = (List) o;
            lo.add(value);
            
            content.put(key, lo);
        } else {
            throw new IllegalArgumentException();
        }
        return this;
    }
    
    public String toCompactedString() throws JsonLdError, IOException {
        return JsonUtils.toString(
                JsonLdProcessor.compact(content, context, new JsonLdOptions()));
    }
    
    public String toExpandedString() throws IOException, JsonLdError {
        return JsonUtils.toString(
                JsonLdProcessor.expand(content));
    }
    
}
