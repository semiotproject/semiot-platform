package ru.semiot.services.analyzing.cqels;

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
import javax.inject.Inject;
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
import ru.semiot.services.analyzing.database.DataBase;
import ru.semiot.services.analyzing.wamp.WAMPClient;

public class Engine {

    private final org.slf4j.Logger logger = LoggerFactory
            .getLogger(Engine.class);
    private final String CQELS_HOME = "cqels_home";
    private static Engine engine = null;
    private ExecContext context = null;
    private final String STREAM_ID = "http://example.org/simpletest/test";
    private DefaultRDFStream stream = null;
    private Map<String, OpRouter1> queries = null;
    
    @Inject private DataBase db; 

    private Engine() {
        logger.info("Initialize home directory for cqels");
        File home = new File(CQELS_HOME);
        if (!home.exists()) {
            home.mkdir();
        }
        context = new ExecContext(home.getAbsolutePath(), true);
        stream = new DefaultRDFStream(context, STREAM_ID);
        queries = new HashMap<>();

    }

    public static Engine getInstance() {
        if (engine == null) {
            engine = new Engine();
        }
        return engine;
    }

    public boolean registerQuery(String query) {
        try {
            if (query.toLowerCase().contains("select") || query.toLowerCase().contains("construct")) {
                if (query.toLowerCase().contains("construct")) {
                    ContinuousConstruct cc = context.registerConstruct(query);
                    cc.register(new ConstructListener(context) {

                        @Override
                        public void update(List<Triple> graph) {
                            sendToWamp(getString(graph));
                        }
                    });
                    queries.put(query, cc);
                } else {
                    ContinuousSelect cs = context.registerSelect(query);
                    cs.register(new ContinuousListener() {

                        @Override
                        public void update(Mapping m) {
                            sendToWamp(getString(m));
                        }
                    });
                    queries.put(query, cs);
                }
            }
        }
        catch(com.hp.hpl.jena.query.QueryException e){
            return false;
        }
        return true;
    }

    public void removeAllQueries() {
        for (String s : queries.keySet()) {
            removeQuery(s);
        }
        queries.clear();
    }

    public void removeQuery(String query) {
        if (query != null && !query.isEmpty() && queries.containsKey(query)) {
            logger.debug("Removing query");
            OpRouter1 op = queries.get(query);
            if (op instanceof ContinuousConstruct) {
                context.unregisterConstruct((ContinuousConstruct) op);
            } else {
                context.unregisterSelect((ContinuousSelect) op);
            }
            queries.remove(query, op);
        } else {
            logger.error("Select not found!");
            logger.debug(queries.keySet().toString());
        }
    }

    public void appendData(String msg) {
        Model description = ModelFactory.createDefaultModel().read(
                new StringReader(msg), null, "TURTLE");
        stream.stream(description);
    }

    private void sendToWamp(String message) {
        //logger.info("Get alert! " + message);
        WAMPClient.getInstance().publish(ServiceConfig.config.topicsAlert(), message);
    }

    private String getString(Mapping m) {
        List<Node> list = toNodeList(m);
        String message = "";
        for (Node n : list) {
            message += n.toString() + "\n";
        }
        return message;
    }

    private String getString(List<Triple> list) {
        String message = "";
        for (Triple t : list) {
            message += t.toString() + "\n";
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
