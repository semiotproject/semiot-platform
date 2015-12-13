package ru.semiot.platform.apigateway;

import com.github.jsonldjava.utils.JsonUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.utils.URIUtils;

@Singleton
public class JsonLdContextProviderService {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonLdContextProviderService.class);
    
    private static final String API_DOCUMENTATION_PATH = "/ru/semiot/platform/apigateway/ApiDocumentation.jsonld";
    private static final String ENTRYPOINT_PATH = "/ru/semiot/platform/apigateway/EntryPoint.jsonld";
    private static final String SYSTEM_FRAME_PATH = "/ru/semiot/platform/apigateway/SystemFrame.jsonld";
    private static final String SENSOR_FRAME_PATH = "/ru/semiot/platform/apigateway/SensorFrame.jsonld";
    
    private final Map<String, Context> contexts = new HashMap<>();
    
    public static final String API_DOCUMENTATION_CONTEXT = "API_DOCUMENTATION_CONTEXT";
    public static final String ENTRYPOINT_CONTEXT = "ENTRYPOINT_CONTEXT";
    public static final String SYSTEM_FRAME = "SYSTEM_FRAME";
    public static final String SENSOR_FRAME = "SENSOR_FRAME";

    public JsonLdContextProviderService() {
    }
    
    @PostConstruct
    public void init() {
        try {
            contexts.put(API_DOCUMENTATION_CONTEXT,
                    Context.readContextTemplate(API_DOCUMENTATION_PATH));
            contexts.put(ENTRYPOINT_CONTEXT,
                    Context.readContextTemplate(ENTRYPOINT_PATH));
            contexts.put(SYSTEM_FRAME,
                    Context.readContextTemplate(SYSTEM_FRAME_PATH));
            contexts.put(SENSOR_FRAME,
                    Context.readContextTemplate(SENSOR_FRAME_PATH));
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
    
    public Map<String, Object> getContextAsJsonLd(
            String contextName, URI requestUri) 
            throws IOException {
        return contexts.get(contextName).createAsJsonLd(URIUtils.extractHostName(requestUri));
    }
    
    public String getContextAsString(String contextName, URI requestUri) {
        return contexts.get(contextName).createAsString(URIUtils.extractHostName(requestUri));
    }
    
    private static class Context {

        private static final Pattern HOST = Pattern.compile("\\$\\{HOST\\}");
        private final String template;
        
        public Context(InputStream input) throws IOException {
            this.template = IOUtils.toString(input);
        }
        
        public Map<String, Object> createAsJsonLd(String host) throws IOException {          
            return (Map<String, Object>) JsonUtils.fromString(createAsString(host));
        }
        
        public String createAsString(String host) {
            StringBuilder builder = new StringBuilder(template);
            
            return HOST.matcher(builder).replaceAll(host);
        }
        
        public static Context readContextTemplate(String path) throws IOException {
            return new Context(
                    JsonLdContextProviderService.class.getResourceAsStream(path));
        }
        
    }
    
}
