package ru.semiot.platform.apigateway;

import com.github.jsonldjava.utils.JsonUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.platform.apigateway.utils.MapBuilder;
import ru.semiot.platform.apigateway.utils.URIUtils;

@Singleton
public class ContextProvider {

    private static final Logger logger = LoggerFactory.getLogger(ContextProvider.class);

    private static final String ROOT = "/ru/semiot/platform/apigateway/";
    private static final String RDF_POSTFIX = ".ttl";
    private static final String FRAME_POSTFIX = "-frame.jsonld";
    private final Map<String, String> rdfModels = new HashMap<>();
    private final Map<String, String> frames = new HashMap<>();

    public static final String API_DOCUMENTATION = "ApiDocumentation";
    public static final String ENTRYPOINT = "EntryPoint";
    public static final String SYSTEM_COLLECTION = "SystemCollection";
    public static final String SYSTEM_SINGLE = "SystemSingle";
    public static final String SENSOR_COLLECTION = "SensorCollection";
    public static final String SENSOR_SINGLE = "SensorSingle";
    public static final String OBSERVATIONS_COLLECTION = "ObservationsCollection";
    
    public static final String VAR_ROOT_URL = "${ROOT_URL}";
    public static final String VAR_SYSTEM_ID = "${SYSTEM_ID}";

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
        return getRDFModel(name, MapBuilder.newMap()
                .put(VAR_ROOT_URL, URIUtils.extractRootURL(root))
                .build());
    }

    public Model getRDFModel(String name, Map<String, Object> vars) {
        String rdf = resolveVars(rdfModels.get(name), vars);
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(rdf), null, Lang.TURTLE);

        return model;
    }

    public Map<String, Object> getFrame(String name, URI root)
            throws IOException {
        String str = frames.get(name).replace(VAR_ROOT_URL,
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

    private String resolveVars(String template, Map<String, Object> vars) {
        return StringUtils.replaceEach(template,
                vars.keySet().toArray(new String[0]),
                vars.values().stream().map((value) -> {
                    return value.toString();
                }).toArray(String[]::new));
    }

}
