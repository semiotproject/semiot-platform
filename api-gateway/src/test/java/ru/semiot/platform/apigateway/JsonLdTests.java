package ru.semiot.platform.apigateway;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import ru.semiot.platform.apigateway.utils.JsonLdBuilder;

public class JsonLdTests {

    @Test
    public void createSimpleJsonLdWithJsonlDBuilder() throws JsonLdError, IOException {
        Map<String, Object> CONTEXT = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>("hydra", "http://www.w3.org/ns/hydra/core#")
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

        JsonLdBuilder builder = new JsonLdBuilder(CONTEXT)
                .add("@id", "http://example.com/a#")
                .add("@type", "hydra:ApiDocumentation")
                .add("hydra:entrypoint", "http://example.com/api");

        System.out.println(builder.toCompactedString());
    }

    @Test
    public void createSimpleJsonLdWithMap() throws JsonLdError, IOException {
        Map<String, Object> CONTEXT = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>("hydra", "http://www.w3.org/ns/hydra/core#")
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
        
        Map<String, Object> content = new HashMap<>();
        content.put("@id", "http://example.com/a#");
        content.put("@type", "ApiDocumentation");
        content.put("entrypoint", "http://example.com/api");

        System.out.println(JsonUtils.toString(JsonLdProcessor.compact(
                content, CONTEXT, new JsonLdOptions())));
    }
    
    @Test
    public void createSimpleJsonLdWithList() throws JsonLdError, IOException {
        Map<String, Object> CONTEXT = Collections.unmodifiableMap(Stream.of(
                new AbstractMap.SimpleEntry<>("hydra", "http://www.w3.org/ns/hydra/core#")
        ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

        JsonLdBuilder builder = new JsonLdBuilder(CONTEXT)
                .add("@id", "http://example.com/a#")
                .add("@type", "hydra:ApiDocumentation")
                .add("hydra:entrypoint", "http://example.com/api")
                .add("hydra:member", new HashMap<>());

        System.out.println(builder.toCompactedString());
    }
}
