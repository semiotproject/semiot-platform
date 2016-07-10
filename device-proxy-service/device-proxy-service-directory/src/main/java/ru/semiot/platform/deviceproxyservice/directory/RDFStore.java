package ru.semiot.platform.deviceproxyservice.directory;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import ru.semiot.platform.deviceproxyservice.api.drivers.Configuration;

public class RDFStore {

  private final HttpAuthenticator httpAuthenticator;
  private final Configuration configuration;

  public RDFStore(Configuration configuration) {
    this.configuration = configuration;
    httpAuthenticator = new SimpleAuthenticator(
        configuration.getAsString(Keys.TRIPLESTORE_USERNAME),
        configuration.getAsString(Keys.TRIPLESTORE_PASSWORD).toCharArray());
  }

  public void save(Model model) {
    DatasetAccessorFactory
        .createHTTP(configuration.getAsString(Keys.TRIPLESTORE_STORE_URL), httpAuthenticator)
        .add(model);
  }

  public void save(String graphUri, Model model) {
    DatasetAccessorFactory
        .createHTTP(configuration.getAsString(Keys.TRIPLESTORE_STORE_URL), httpAuthenticator)
        .add(graphUri, model);
  }

  public ResultSet select(String query) {
    return select(QueryFactory.create(query));
  }

  public ResultSet select(Query query) {
    Query select = QueryFactory.create(query);
    ResultSet rs = QueryExecutionFactory
        .createServiceRequest(
            configuration.getAsString(Keys.TRIPLESTORE_QUERY_URL),
            select,
            httpAuthenticator)
        .execSelect();
    return rs;
  }

  public void update(String update) {
    UpdateRequest updateRequest = UpdateFactory.create(update);

    UpdateExecutionFactory.createRemote(
        updateRequest,
        configuration.getAsString(Keys.TRIPLESTORE_UPDATE_URL), httpAuthenticator)
        .execute();
  }

}
