package ru.semiot.platform.deviceproxyservice.manager;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;

public class RDFStore {

    private static final String PREFIXES = ""
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
    private final HttpAuthenticator httpAuthenticator;
    private final Configuration config;

    public RDFStore(Configuration config) {
        this.config = config;
        httpAuthenticator = new SimpleAuthenticator(
                config.get(Keys.FUSEKI_USERNAME),
                config.get(Keys.FUSEKI_PASSWORD).toCharArray());
    }

    public void save(Model model) {
        DatasetAccessorFactory
                .createHTTP(
                        config.get(Keys.FUSEKI_STORE_URL),
                        httpAuthenticator)
                .add(model);
    }
    
    public void save(String graphUri, Model model) {
        DatasetAccessorFactory.createHTTP(
                config.get(Keys.FUSEKI_STORE_URL), 
                httpAuthenticator)
                .add(graphUri, model);
    }
    
    public ResultSet select(String query) {
        return select(QueryFactory.create(query));
    }

    public ResultSet select(Query query) {
        Query select = QueryFactory.create(query);
        ResultSet rs = QueryExecutionFactory
                .createServiceRequest(
                        config.get(Keys.FUSEKI_QUERY_URL),
                        select,
                        httpAuthenticator)
                .execSelect();
        return rs;
    }

    public void update(String update) {
        UpdateRequest updateRequest = UpdateFactory.create(update);

        UpdateExecutionFactory.createRemote(
                updateRequest,
                config.get(Keys.FUSEKI_UPDATE_URL),
                httpAuthenticator)
                .execute();
    }

    private String modelToQuery(Model model) throws IOException {
        String query = PREFIXES + "INSERT DATA {\n";
        try (StringWriter writer = new StringWriter()) {
            model.write(writer, RDFLanguages.N3.getName());
            query += writer.toString();
        }
        query += "\n}";
        return query;
    }

}
