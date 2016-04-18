package ru.semiot.platform.apigateway.beans.impl;

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
    private static final String TTL_POSTFIX = ".ttl";
    private static final String FRAME_POSTFIX = "-frame.jsonld";
    private static final String JSONLD_POSTFIX = ".jsonld";
    private final Map<String, String> rdfModels = new HashMap<>();
    private final Map<String, String> frames = new HashMap<>();
    private final Map<String, String> contexts = new HashMap<>();

    public static final String COMMON_CONTEXT = "CommonContext";
    public static final String API_DOCUMENTATION = "ApiDocumentation";
    public static final String ENTRYPOINT = "EntryPoint";
    public static final String ACTUATION = "Actuation";
    public static final String SYSTEM_COLLECTION = "SystemCollection";
    public static final String SYSTEM_SINGLE = "SystemSingle";
    public static final String SENSOR_COLLECTION = "SensorCollection";
    public static final String SUBSYSTEM_SINGLE = "SubSystemSingle";
    public static final String SYSTEM_OBSERVATIONS_COLLECTION = "SystemObservationsCollection";
    public static final String SYSTEM_OBSERVATIONS_PARTIAL_COLLECTION = "SystemObservationsCollection-Partial";
    public static final String SYSTEM_ACTUATIONS_COLLECTION = "SystemActuationsCollection";
    public static final String SYSTEM_ACTUATIONS_PARTIAL_COLLECTION = "SystemActuationsCollection-Partial";

    public static final String VAR_ROOT_URL = "${ROOT_URL}";
    public static final String VAR_SYSTEM_ID = "${SYSTEM_ID}";
    public static final String VAR_SUBSYSTEM_ID = "${SUBSYSTEM_ID}";
    public static final String VAR_WAMP_URL = "${WAMP_URL}";
    public static final String VAR_QUERY_PARAMS = "${QUERY_PARAMS}";

    public ContextProvider() {
    }

    @PostConstruct
    public void init() {
        try {
            loadModelAndFrame(API_DOCUMENTATION);
            loadModelAndFrame(ENTRYPOINT);
            loadModelAndFrame(SYSTEM_COLLECTION);
            loadModelAndFrame(SYSTEM_SINGLE);
            loadModelAndFrame(SENSOR_COLLECTION);
            loadModelAndFrame(SUBSYSTEM_SINGLE);
            loadModelAndFrame(SYSTEM_OBSERVATIONS_COLLECTION);
            loadModelAndFrame(SYSTEM_ACTUATIONS_COLLECTION);

            loadContext(COMMON_CONTEXT);

            loadFrame(SYSTEM_OBSERVATIONS_PARTIAL_COLLECTION);
            loadFrame(SYSTEM_ACTUATIONS_PARTIAL_COLLECTION);
            loadFrame(ACTUATION);
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

    public String getContext(String name, URI root) {
        String str = contexts.get(name).replace(VAR_ROOT_URL,
                URIUtils.extractRootURL(root));

        return str;
    }

    private void loadFrame(String name) throws IOException {
        frames.put(name, readFile(ROOT + name + FRAME_POSTFIX));
    }

    private void loadModelAndFrame(String name) throws IOException {
        rdfModels.put(name, readFile(ROOT + name + TTL_POSTFIX));
        frames.put(name, readFile(ROOT + name + FRAME_POSTFIX));
    }

    private void loadContext(String name) throws IOException {
        contexts.put(name, readFile(ROOT + name + JSONLD_POSTFIX));
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
                    return String.valueOf(value);
                }).toArray(String[]::new));
    }

}