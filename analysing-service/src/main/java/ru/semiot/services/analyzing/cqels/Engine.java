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
import ru.semiot.services.analyzing.wamp.WAMPClient;

public class Engine {

    private static final org.slf4j.Logger logger = LoggerFactory
            .getLogger(Engine.class);
    private static final String CQELS_HOME = "cqels_home";
    private static ExecContext context = null;
    private static final String STREAM_ID = "http://example.org/simpletest/test";
    private static DefaultRDFStream stream = null;
    private static boolean init = false;
    private static Map<String, OpRouter1> selects = null;

    public static void beforeClass() {
        if (!init) {
            logger.info("Initialize home directory for cqels");
            File home = new File(CQELS_HOME);
            if (!home.exists()) {
                home.mkdir();
            }
            context = new ExecContext(home.getAbsolutePath(), true);
            stream = new DefaultRDFStream(context, STREAM_ID);
            selects = new HashMap<>();
            init = true;
        }
    }

    public static void destroy() {
        logger.info("Destroy home directory for cqels");
        for (String s : selects.keySet()) {
            removeSelect(s);
        }
        selects = null;
        context = null;
        stream = null;
        File home = new File(CQELS_HOME);
        if (home.exists()) {
            home.delete();
        }
    }

    public static void registerSelect(String select) {

        if (select.toLowerCase().contains("select") || select.toLowerCase().contains("construct")) {
            if (select.toLowerCase().contains("construct")) {
                ContinuousConstruct cc = context.registerConstruct(select);
                cc.register(new ConstructListener(context) {

                    @Override
                    public void update(List<Triple> graph) {

                    }
                });
                selects.put(select, cc);
            } else {
                ContinuousSelect cs = context.registerSelect(select);
                cs.register(new ContinuousListener() {

                    @Override
                    public void update(Mapping m) {
                        sendToWamp(getString(m));
                    }
                });
                selects.put(select, cs);
            }
        }
    }

    public static void removeAllSelects() {
        for (String s : selects.keySet()) {
            removeSelect(s);
        }
        selects.clear();
    }

    public static void removeSelect(String select) {
        if (selects != null && !selects.isEmpty() && selects.containsKey(select)) {
            logger.debug("Removing select");
            OpRouter1 op = selects.get(select);
            if (op instanceof ContinuousConstruct) {

            } else {
                context.unregisterSelect((ContinuousSelect) op);
            }
            selects.remove(select, op);
        }
        else{
            logger.error("Select not found!");
            logger.debug(selects.keySet().toString());
        }
    }

    public static void appendData(String msg) {
        Model description = ModelFactory.createDefaultModel().read(
                new StringReader(msg), null, "TURTLE");
        stream.stream(description);
    }

    private static void sendToWamp(String message) {
        //logger.info("Get alert! " + message);
        WAMPClient.getInstance().publish(ServiceConfig.config.topicsAlert(), message);
    }

    private static String getString(Mapping m) {
        List<Node> list = toNodeList(m);
        String message = "";
        for (Node n : list) {
            message += n.toString() + "\n";
        }
        return message;
    }

    private static List<Node> toNodeList(Mapping mapping) {
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
