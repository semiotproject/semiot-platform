package ru.semiot.services.analyzing.cep;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.QueryParseException;
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
import java.text.ParseException;
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
import org.slf4j.LoggerFactory;
import ru.semiot.services.analyzing.ServiceConfig;
import ru.semiot.services.analyzing.database.EventsDataBase;
import ru.semiot.services.analyzing.database.QueryDataBase;
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

    public CSPARQL() {
        engine = new CsparqlEngineImpl();
        stream = new RdfStream(STREAM_URI);
        engine.initialize();
        engine.registerStream(stream);
        queries = new HashMap<>();
        logger.info("C-SPARQL is initialized");
    }
    @Inject
    QueryDataBase db;
    @Inject
    EventsDataBase dbe;

    @Override
    public void appendData(String message) {
        Model description = ModelFactory.createDefaultModel().read(
                new StringReader(message), null, "TURTLE");
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
                        sendToWAMP(getString(vars, bindings));
                        appendEventsToStore(getString(vars, bindings), query_id);
                    }
                });
                return true;
            } else {
                return false;
            }

        } catch (ParseException | QueryParseException | NullPointerException ex) {
            logger.debug("Bad exception with message: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public void removeQuery(int query_id) {
        if (queries.containsKey(query_id)) {
            logger.info("Removing query");
            engine.unregisterQuery(queries.get(query_id).getId());
            queries.remove(query_id);
        } else {
            logger.error("Query not found!");
            logger.debug(queries.keySet().toString());
        }

    }

    public void sendToWAMP(String message) {
        logger.info("Get alert!\n " + message);
        WAMPClient.getInstance().publish(ServiceConfig.config.topicsAlert(), message);
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
        if (value.startsWith("http://")) {
            return NodeFactory.createURI(value);
        } else {
            if (value.contains("^^")) {
                String[] parts = value.split("\\^\\^");
                RDFDatatype dtype = NodeFactory.getType(toUri(parts[1]));
                return NodeFactory.createLiteral(unquoting(parts[0]), dtype);
            } else {
                return NodeFactory.createLiteral(value);
            }
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
            dbe.appendEvents(query_id, events);
        }
    }

    private String asURI(final String string) {
        final StringBuilder builder = new StringBuilder();
        if (string.startsWith("http://")) {
            //If it is URI (something like http://ex.com/#hasValue)
            builder.append(LT).append(string).append(GT);
        } else {
            if (string.contains("^^")) {
                //If it is literal (something like 1^^http://www.w3.org/2001/XMLSchema#integer or text^^http://www.w3.org/2001/XMLSchema#string)
                String[] parts = string.split("\\^\\^");
                builder.append(QUOTE).append(parts[0]).append(QUOTE).append("^^").append(LT).append(parts[1]).append(GT);
            } else {
                builder.append(string);
            }
        }
        return builder.toString();
    }
}
