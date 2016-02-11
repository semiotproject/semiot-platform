package ru.semiot.platform.apigateway;

import com.github.jsonldjava.utils.JsonUtils;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.utils.URIUtils;

@Singleton
public class ContextProvider {

    private static final Logger logger = LoggerFactory.getLogger(ContextProvider.class);

    private static final String ROOT = "/ru/semiot/platform/apigateway/";
    private static final String RDF_POSTFIX = ".ttl";
    private static final String FRAME_POSTFIX = "-frame.jsonld";
    private static final String ROOT_URL = "${ROOT_URL}";
    private final Map<String, String> rdfModels = new HashMap<>();
    private final Map<String, String> frames = new HashMap<>();

    public static final String API_DOCUMENTATION = "ApiDocumentation";
    public static final String ENTRYPOINT = "EntryPoint";
    public static final String SYSTEM_COLLECTION = "SystemCollection";
    public static final String SYSTEM_SINGLE = "SystemSingle";
    public static final String SENSOR_COLLECTION = "SensorCollection";
    public static final String SENSOR_SINGLE = "SensorSingle";

    public ContextProvider() {
    }

    @PostConstruct
    public void init() {
        try {
            loadContext(API_DOCUMENTATION);
            loadContext(ENTRYPOINT);
            loadContext(SYSTEM_COLLECTION);
            loadContext(SENSOR_COLLECTION);
            
            loadFrame(SYSTEM_SINGLE);
            loadFrame(SENSOR_SINGLE);
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public Model getRDFModel(String name, URI root) {
        String str = rdfModels.get(name).replace(ROOT_URL, 
                URIUtils.extractRootURL(root));
        
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(str), null, Lang.TURTLE);
        
        return model;
    }

    public Map<String, Object> getFrame(String name, URI root) 
            throws IOException {
        String str = frames.get(name).replace(ROOT_URL, 
                URIUtils.extractRootURL(root));
        
        return (Map<String, Object>) JsonUtils.fromString(str);
    }
    
    private void loadFrame(String name) throws IOException {
        frames.put(name, readFile(ROOT + name + FRAME_POSTFIX));
    }

    private void loadContext(String name) throws IOException {
        rdfModels.put(name, readFile(ROOT + name + RDF_POSTFIX));
        frames.put(name, readFile(ROOT + name + FRAME_POSTFIX));
    }

    private String readFile(String path) throws IOException {
        InputStream stream = this.getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("File " + path + " doesn't exist!");
        }
        return IOUtils.toString(stream);
    }

}
