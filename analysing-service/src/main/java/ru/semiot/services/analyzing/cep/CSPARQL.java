package ru.semiot.services.analyzing.cep;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.core.engine.CsparqlEngine;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.DatatypeConverter;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import static ru.semiot.services.analyzing.ServiceConfig.config;
import ru.semiot.services.analyzing.database.EventsDataBase;
import ru.semiot.services.analyzing.database.QueryDataBase;
import ru.semiot.services.analyzing.wamp.Subsciber;
import ru.semiot.services.analyzing.wamp.WAMPClient;

@Named
@Default
@ApplicationScoped
public class CSPARQL implements Engine {

  private final org.slf4j.Logger logger = LoggerFactory
      .getLogger(CSPARQL.class);
  private final String STREAM_URI = "http://ex.org/streams/test";
  private final CsparqlEngine engine;
  private final RdfStream stream;
  private final Map<Integer, CsparqlQueryResultProxy> queries;
  private final HttpAuthenticator httpAuthenticator;

  public CSPARQL() {
    engine = new CsparqlEngineImpl();
    stream = new RdfStream(STREAM_URI);
    engine.initialize();
    engine.registerStream(stream);
    queries = new HashMap<>();
    httpAuthenticator = new SimpleAuthenticator(config.storeUsername(),
        config.storePassword().toCharArray());
    logger.info("C-SPARQL is initialized");
  }
  @Inject
  QueryDataBase db;
  @Inject
  EventsDataBase dbe;
  @Inject
  Subsciber subscriber;

  @Override
  public void appendData(Model description) {
    StmtIterator iterator = description.listStatements();
    String timestamp = description.getGraph()
        .find(Node.ANY, NodeFactory.createURI("http://purl.oclc.org/NET/ssnx/ssn#observationResultTime"), Node.ANY)
        .next().getObject().getLiteralValue().toString();
    Calendar calendar = DatatypeConverter.parseDateTime(timestamp);
    while (iterator.hasNext()) {
      Statement stmt = iterator.next();
      stream.put(new RdfQuadruple(stmt.getSubject().getURI(), stmt.getPredicate().getURI(), stmt.getObject().toString(), calendar.getTimeInMillis()));
    }
  }

  @Override
  public boolean registerQuery(int query_id) {
    String query = db.getQuery(query_id).getString("text");
    if (!subscribeTopics(query_id, true)) {
      return false;
    }
    try {
      logger.debug("Try to register query:\n" + query);
      CsparqlQueryResultProxy proxy = engine.registerQuery(query, false);
      if (proxy != null) {
        queries.put(query_id, proxy);
        logger.debug("Query is appended. Try to append Observer");
        proxy.addObserver(new Observer() {

          @Override
          public void update(Observable o, Object arg) {
            final RDFTable rdfTable = (RDFTable) arg;
            final List<Binding> bindings = new ArrayList<>();
            final String[] vars = rdfTable.getNames().toArray(new String[]{});
            rdfTable.stream().forEach((t) -> {
              bindings.add(toBinding(vars, t.toString(), "\t"));
            });
            sendToWAMP(getString(vars, bindings), query_id);
            appendEventsToStore(getString(vars, bindings), query_id);
            //subscribeTopics(query_id, true);
          }
        });
        return true;
      } else {
        return false;
      }

    } catch (Exception ex) {
      logger.debug("Error in C-SPARQL query! Message: " + ex.getMessage());
      subscribeTopics(query_id, false);
      return false;
    }
  }

  private boolean subscribeTopics(int query_id, boolean subsc) {
    try {
      String sparql = db.getQuery(query_id).getString("sparql");
      QueryExecution sparqlService = QueryExecutionFactory.sparqlService(config.storeUrl(), sparql);
      sparqlService.setTimeout(600000);
      ResultSet execution = sparqlService.execSelect();
      List<String> lst = execution.getResultVars();
      if (lst.isEmpty() || lst.size() > 1) {
        logger.debug("Sparql query is bad!");
        return false;
      }
      String var = lst.get(0);
      List<String> topics = new ArrayList<>();
      while (execution.hasNext()) {
        topics.add(execution.next().get(var).asLiteral().getString());
      }
      if (topics.isEmpty()) {
        return false;
      }
      if (subsc) {
        subscriber.subscribeTopics(topics, query_id);
      } else {
        subscriber.unsubscribeTopics(topics, query_id);
      }
      return true;
    } catch (Throwable ex) {
      logger.debug("Error in sparql query! Message: " + ex.getMessage());
      return false;
    }
  }

  @Override
  public void removeQuery(int query_id) {
    if (queries.containsKey(query_id)) {
      logger.info("Removing query");
      engine.unregisterQuery(queries.get(query_id).getId());
      subscribeTopics(query_id, false);
      queries.remove(query_id);
    } else {
      logger.error("Query not found!");
      logger.debug(queries.keySet().toString());
    }

  }

  public void sendToWAMP(String message, int query_id) {
    logger.debug("Get alert!\n " + message);
    WAMPClient.getInstance().publish(config.topicsAlert() + "." + query_id, message);
  }

  private String getString(String[] vars, List<Binding> bindings) {
    StringBuilder message = new StringBuilder();
    for (Binding binding : bindings) {
      for (String varName : vars) {
        final Var var = Var.alloc(varName);
        message.append(asURI(binding.get(var).toString(false))).append("\t");
      }
      message.append('\n');
    }
    return message.toString();
  }

  private final String QUOTE = "\"";
  private final String GT = ">";
  private final String LT = "<";

  private Binding toBinding(String[] vars, String string, String separator) {
    String[] values = string.split(separator);
    final BindingMap binding = BindingFactory.create();
    for (int i = 0; i < vars.length; i++) {
      binding.add(Var.alloc(vars[i]), toNode(values[i]));
    }
    return binding;
  }

  private Node toNode(String value) {
    if (value.startsWith("http://") || value.startsWith("https://")) {
      return NodeFactory.createURI(value);
    } else if (value.contains("^^")) {
      String[] parts = value.split("\\^\\^");
      RDFDatatype dtype = NodeFactory.getType(toUri(parts[1]));
      return NodeFactory.createLiteral(unquoting(parts[0]), dtype);
    } else {
      return NodeFactory.createLiteral(value);
    }
  }

  private String unquoting(final String string) {
    final StringBuilder builder = new StringBuilder(string);
    if (builder.indexOf(QUOTE) == 0) {
      builder.deleteCharAt(0);
    }
    if (builder.lastIndexOf(QUOTE) == builder.length() - 1) {
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  private String toUri(final String string) {
    final StringBuilder builder = new StringBuilder(string);
    if (builder.indexOf(LT) == 0) {
      builder.deleteCharAt(0);
    }
    if (builder.lastIndexOf(GT) == builder.length() - 1) {
      builder.deleteCharAt(builder.length() - 1);
    }
    return builder.toString();
  }

  private void appendEventsToStore(String events, int query_id) {
    if (events != null && !events.isEmpty()) {
      JSONArray array = toJSONfromRDF(events);
      dbe.appendEvents(query_id, array.toString());
    }
  }

  private JSONArray toJSONfromRDF(String message) {
    Model description = ModelFactory.createDefaultModel().read(
        new StringReader(message.replaceAll("\t\n", ".\n")), null, "TURTLE");

    JSONArray array = new JSONArray();
    JSONObject sensor;
    List<Triple> sensorsList = description.getGraph()
        .find(Node.ANY, NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), NodeFactory.createURI("http://example.com/#Diff"))
        .toList();

    for (Triple t : sensorsList) {
      sensor = new JSONObject();
      String sensorURI = t.getSubject().getURI();
      String diff = description.getGraph().find(t.getSubject(), NodeFactory.createURI("http://example.com/#hasDiff"), Node.ANY)
          .next().getObject().getLiteral().getValue().toString();
      String absTemp = description.getGraph().find(t.getSubject(), NodeFactory.createURI("http://example.com/#hasAbsTemp"), Node.ANY)
          .next().getObject().getLiteral().getValue().toString();
      String absAvg = description.getGraph().find(t.getSubject(), NodeFactory.createURI("http://example.com/#hasAvg"), Node.ANY)
          .next().getObject().getLiteral().getValue().toString();
      String group = description.getGraph().find(t.getSubject(), NodeFactory.createURI("http://example.com/#InGroup"), Node.ANY)
          .next().getObject().getLiteral().getValue().toString();
      sensor.put("sensor", sensorURI);
      sensor.put("group", Integer.parseInt(group));
      sensor.put("avg", Double.parseDouble(absAvg));
      sensor.put("diff", Double.parseDouble(diff));
      sensor.put("temp", Double.parseDouble(absTemp));
      array.put(sensor);
    }
    return array;
  }

  private String asURI(final String string) {
    final StringBuilder builder = new StringBuilder();
    if (string.startsWith("http://") || string.startsWith("https://")) {
      //If it is URI (something like http://ex.com/#hasValue)
      builder.append(LT).append(string).append(GT);
    } else if (string.contains("^^")) {
      //If it is literal (something like 1^^http://www.w3.org/2001/XMLSchema#integer or text^^http://www.w3.org/2001/XMLSchema#string)
      String[] parts = string.split("\\^\\^");
      builder.append(QUOTE).append(parts[0]).append(QUOTE).append("^^").append(LT).append(parts[1]).append(GT);
    } else {
      builder.append(string);
    }
    return builder.toString();
  }
}
