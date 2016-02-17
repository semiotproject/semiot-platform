package ru.semiot.platform.apigateway;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;
import com.github.jsonldjava.utils.Obj;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.platform.apigateway.utils.JsonLdBuilder;
import ru.semiot.platform.apigateway.utils.JsonLdKeys;

public class JsonLdBuilderTest {

    public JsonLdBuilderTest() {
    }

    @Test
    public void testAppend() throws IOException, JsonLdError {
//        Map<String, Object> apiDoc = (Map<String, Object>) JsonUtils.fromInputStream(
//                this.getClass().getResourceAsStream("/JsonLdBuilderTest/ApiDocumentation.jsonld"));
//
//        JsonLdBuilder builder = new JsonLdBuilder()
//                .context(apiDoc)
//                .append(apiDoc);
//
//        builder.append(Hydra.supportedClassString, Obj.newMap(JsonLdKeys.ID, "_:testClass"));
//
//        Map<String, Object> object = builder.toJsonLdObject();
//        
//        assertTrue(object.containsKey(Hydra.supportedClassString));
//
//        long actual = ((List<Object>) object.get(Hydra.supportedClassString))
//                .stream().filter((Object o) -> {
//                    return ((Map<String, Object>) o).get(JsonLdKeys.ID).equals("_:testClass");
//                }).count();
//        
//        assertEquals(1, actual);
    }

}
