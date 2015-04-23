package ru.semiot.cqels;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Var;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.engine.ContinuousListener;
import org.deri.cqels.engine.ExecContext;
import org.deri.cqels.engine.RDFStream;
import static ru.semiot.WAMP.ServiceConfig.config;
import ru.semiot.WAMP.WAMPClient;

/**
 *
 * @author Даниил
 */
public class Engine {

    private static final String CQELS_HOME = "cqels_home";
    private static ExecContext context = null;
    private static final String STREAM_ID = "http://example.org/simpletest/test";
    private static DefaultRDFStream stream = null;
    private static boolean init = false;

    public static void beforeClass() {
        if (!init) {
            File home = new File(CQELS_HOME);
            if (!home.exists()) {
                home.mkdir();
            }
            context = new ExecContext(home.getAbsolutePath(), true);
            stream = new DefaultRDFStream(context, STREAM_ID);
            init = true;
        }
    }

    public static void registerSelect(String select) {
        context.registerSelect(select).register(new ContinuousListener() {

            @Override
            public void update(Mapping m) {
                WAMPClient.getInstance().publish(config.topicsAlert(), getString(m));
            }
        });
    }

    public static void appendData(Model msg) {
        
        stream.stream(msg);
    }

    public static void writeFile(String msg) {
        try {
            FileWriter f = new FileWriter("qwe.txt");
            f.write(msg);
            f.close();
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }

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
