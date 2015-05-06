package ru.semiot.services.devicedirectory;

import java.io.IOException;
import java.io.StringWriter;

import org.aeonbits.owner.ConfigFactory;
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
	private static final ServiceConfig config = ConfigFactory
			.create(ServiceConfig.class);
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

	public ResultSet select(String query) {
		Query select = QueryFactory.create(query);
		ResultSet rs = QueryExecutionFactory.createServiceRequest(
				config.storeUrl(), select, httpAuthenticator).execSelect();
		return rs;
	}

	public void update(String update) {
		// ResultSet result = QueryExecutionFactory.createServiceRequest(
		// config.storeUrl(), query, httpAuthenticator).execSelect();
		UpdateRequest updateRequest = UpdateFactory.create(update);

		UpdateExecutionFactory.createRemote(updateRequest, config.storeUrl(),
				httpAuthenticator).execute();
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
