package ru.semiot.platform.deviceproxyservice.manager;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;

import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

public class RDFStore {

    private static final String PREFIXES = ""
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
    private final HttpAuthenticator httpAuthenticator;
    private DeviceManagerImpl dmi;

    public RDFStore(DeviceManagerImpl dmi) {
        this.dmi = dmi;
        httpAuthenticator = new SimpleAuthenticator(
                dmi.getConfiguration().get(Keys.FUSEKI_USERNAME),
                dmi.getConfiguration().get(Keys.FUSEKI_PASSWORD).toCharArray());
    }

    public void save(Model model) {
        DatasetAccessorFactory
                .createHTTP(
                        dmi.getConfiguration().get(Keys.FUSEKI_STORE_URL),
                        httpAuthenticator)
                .add(model);
    }
    
    public ResultSet select(String query) {
        return select(QueryFactory.create(query));
    }

    public ResultSet select(Query query) {
        Query select = QueryFactory.create(query);
        ResultSet rs = QueryExecutionFactory
                .createServiceRequest(
                        dmi.getConfiguration().get(Keys.FUSEKI_QUERY_URL),
                        select,
                        httpAuthenticator)
                .execSelect();
        return rs;
    }

    public void update(String update) {
        UpdateRequest updateRequest = UpdateFactory.create(update);

        UpdateExecutionFactory.createRemote(
                updateRequest,
                dmi.getConfiguration().get(Keys.FUSEKI_UPDATE_URL),
                httpAuthenticator)
                .execute();
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
