package ru.semiot.services.analyzing.cep;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Var;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.engine.ConstructListener;
import org.deri.cqels.engine.ContinuousConstruct;
import org.deri.cqels.engine.ContinuousListener;
import org.deri.cqels.engine.ContinuousSelect;
import org.deri.cqels.engine.ExecContext;
import org.deri.cqels.engine.OpRouter1;
import org.deri.cqels.engine.RDFStream;
import org.slf4j.LoggerFactory;
import ru.semiot.services.analyzing.ServiceConfig;
import ru.semiot.services.analyzing.database.QueryDataBase;
import ru.semiot.services.analyzing.wamp.WAMPClient;

@Named
@Alternative
@ApplicationScoped
public class CQELS implements Engine {

    private final org.slf4j.Logger logger = LoggerFactory
            .getLogger(CQELS.class);
    private final String CQELS_HOME = "cqels_home";
    private static CQELS engine = null;
    private ExecContext context = null;
    private final String STREAM_ID = "http://example.org/simpletest/test";
    private DefaultRDFStream stream = null;
    private Map<Integer, OpRouter1> queries = null;
    //private Map<String, DefaultRDFStream> streams = null;
    @Inject
    QueryDataBase db;

    public CQELS() {
        logger.info("Initialize home directory for cqels");
        File home = new File(CQELS_HOME);
        if (!home.exists()) {
            home.mkdir();
        }
        context = new ExecContext(home.getAbsolutePath(), true);
        stream = new DefaultRDFStream(context, STREAM_ID);
        //streams = new HashMap<>();
        queries = new HashMap<>();

    }

    @Override
    public boolean registerQuery(int query_id) {
        String query = db.getQuery(query_id).getString("text");
        try {
            if (query.toLowerCase().contains("select") || query.toLowerCase().contains("construct")) {
                if (query.toLowerCase().contains("construct")) {
                    ContinuousConstruct cc = context.registerConstruct(query);
                    cc.register(new ConstructListener(context) {

                        @Override
                        public void update(List<Triple> graph) {
                            String message = getString(graph);
                            sendToWAMP(message);
                            appendData(message);
                        }
                    });
                    queries.put(query_id, cc);
                } else {
                    ContinuousSelect cs = context.registerSelect(query);
                    cs.register(new ContinuousListener() {

                        @Override
                        public void update(Mapping m) {
                            sendToWAMP(getString(m));
                        }
                    });
                    queries.put(query_id, cs);
                }
            }
        } catch (com.hp.hpl.jena.query.QueryException e) {
            logger.debug("Query \n" + query + "\n has ERROR!\n + " + e.getMessage());
            return false;
        }
        return true;
    }

    public void removeAllQueries() {
        for (Integer s : queries.keySet()) {
            removeQuery(s);
        }
        queries.clear();
    }

    @Override
    public void removeQuery(int query_id) {
        if (queries.containsKey(query_id)) {
            logger.debug("Removing query");
            OpRouter1 op = queries.get(query_id);
            if (op instanceof ContinuousConstruct) {
                context.unregisterConstruct((ContinuousConstruct) op);
            } else {
                context.unregisterSelect((ContinuousSelect) op);
            }
            queries.remove(query_id, op);
        } else {
            logger.error("Select not found!");
            logger.debug(queries.keySet().toString());
        }
    }

    @Override
    public void appendData(String msg) {
        Model description = ModelFactory.createDefaultModel().read(
                new StringReader(msg), null, "TURTLE");
        stream.stream(description);
        /*
         String streamName = description.getNsPrefixURI("");
         if(!streams.containsKey(streamName))
         streams.put(streamName, new DefaultRDFStream(context, streamName));
         streams.get(streamName).stream(description);        
         */
    }

    public void sendToWAMP(String message) {
        logger.info("Get alert! " + message);
        WAMPClient.getInstance().publish(ServiceConfig.config.topicsAlert(), message);
    }

    private String getString(Mapping m) {
        List<Node> list = toNodeList(m);
        String message = "";
        for (Node n : list) {
            if (n != null) {
                message += n.toString() + "\n";
            }
        }
        return message;
    }

    private String getString(List<Triple> list) {
        String message = "";
        String object;
        for (Triple t : list) {
            if (t != null) {
                if (t.getObject().isURI()) {
                    object = "<" + t.getObject().getURI() + ">";
                } else {
                    object = t.getObject().toString();
                    object = object.replace("^^", "^^<").concat(">");
                }

                message += "<" + t.getSubject().getURI() + "> <"
                        + t.getPredicate().getURI() + "> "
                        + object + " .\n";
                //message += t.toString() + "\n";
            }
        }
        return message;
    }

    private List<Node> toNodeList(Mapping mapping) {
        List<Node> nodes = new ArrayList<Node>();
        for (Iterator<Var> vars = mapping.vars(); vars.hasNext();) {
            final long id = mapping.get(vars.next());
            if (id > 0) {
                nodes.add(context.engine().decode(id));
            } else {
                nodes.add(null);
            }
        }
        return nodes;
    }

    private static class DefaultRDFStream extends RDFStream {

        public DefaultRDFStream(ExecContext context, String uri) {
            super(context, uri);
        }

        public void stream(Model t) {
            StmtIterator iter = t.listStatements();
            Triple trip;
            Node sub, obj, pred;
            while (iter.hasNext()) {
                trip = iter.next().asTriple();
                pred = trip.getPredicate();
                obj = trip.getObject();
                sub = trip.getSubject();
                super.stream(sub, pred, obj);
            }
        }

        @Override
        public void stop() {
        }

    }
}
