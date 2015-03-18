package ru.semiot.services.devicedirectory;

import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.Model;
import java.io.IOException;
import java.io.StringWriter;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;

public class RDFStore {

    private static final String PREFIXES = ""
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
    private static final ServiceConfig config = ConfigFactory.create(
            ServiceConfig.class);
    private static final RDFStore INSTANCE = new RDFStore();
    private final HttpAuthenticator httpAuthenticator;

    private RDFStore() {
        httpAuthenticator = new SimpleAuthenticator(config.storeUsername(),
                config.storePassword().toCharArray());
    }

    public static final RDFStore getInstance() {
        return INSTANCE;
    }

    public void save(Model model) {
        DatasetAccessorFactory.createHTTP(config.storeUrl(), httpAuthenticator)
                .add(model);
    }

    private String modelToQuery(Model model) throws IOException {
        String query = PREFIXES + "INSERT DATA {\n";
        try (StringWriter writer = new StringWriter()) {
            model.write(writer, "N3");
            query += writer.toString();
        }
        query += "\n}";
        return query;
    }

}
