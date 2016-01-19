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

    private Map<String, Object> context = new HashMap<>();
    private Map<String, Object> content = new HashMap<>();

    public JsonLdBuilder() {
    }

    public JsonLdBuilder context(Map<String, Object> context) {
        if (context.containsKey(JsonLdKeys.CONTEXT)) {
            Object c = context.get(JsonLdKeys.CONTEXT);
            if(c instanceof Map) {
                this.context = (Map<String, Object>) c;
            } else {
                throw new IllegalArgumentException("Context should be a map!");
            }
        } else {
            this.context = context;
        }

        return this;
    }

    public JsonLdBuilder append(Map<String, Object> content) {
        this.content.putAll(content);

        return this;
    }

    public JsonLdBuilder append(String key, Object value) {
        if (content.containsKey(key)) {
            List<Object> lo;
            Object o = content.get(key);

            if (o instanceof List) {
                lo = (List<Object>) o;
                lo.add(value);
            } else {
                lo = new ArrayList<>();
                lo.add(content.get(key));
                lo.add(value);
            }

            content.put(key, lo);
        } else {
            content.put(key, value);
        }

        return this;
    }

    public Map<String, Object> toJsonLdObject() throws JsonLdError {
        return JsonLdProcessor.compact(content, context, new JsonLdOptions());
    }

    public String toCompactedString() throws JsonLdError, IOException {
        return JsonUtils.toString(
                JsonLdProcessor.compact(content, context, new JsonLdOptions()));
    }

    public String toFlattenString() throws JsonLdError, IOException {
        return JsonUtils.toString(JsonLdProcessor.flatten(content, context, new JsonLdOptions()));
    }

    public String toExpandedString() throws IOException, JsonLdError {
        return JsonUtils.toString(
                JsonLdProcessor.expand(content));
    }

}
