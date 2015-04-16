package ru.semiot.examplecqels;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.core.Var;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.engine.ContinuousListener;
import org.deri.cqels.engine.ExecContext;
import org.deri.cqels.engine.RDFStream;

/**
 *
 * @author Даниил
 */
public class Engine {

    private static final String CQELS_HOME = "cqels_home";
    private static ExecContext context;
    private final String STREAM_ID = "http://example.org/simpletest/test";
    RDFStream stream;
    List<Mapping> maps;
    List<Triple> data;
    List<String> selects;
    Random r = new Random();

    public static void beforeClass() {
        File home = new File(CQELS_HOME);
        if (!home.exists()) {
            home.mkdir();
        }
        context = new ExecContext(CQELS_HOME, true);
    }

    public Engine() {
        maps = new ArrayList<Mapping>();
        data = readFile("triples.ttl");
        stream = new DefaultRDFStream(context, STREAM_ID);
        selects = readRequests("requests.txt");
    }

    public void Test() {
        appendData();
        for(String s : selects)
            registerSelect(s.replace("?{STREAM_ID}", STREAM_ID));
        
    }

    private void registerSelect(String select) {
        context.registerSelect(select).register(new ContinuousListener() {

            public void update(Mapping m) {
                print(m);
                maps.add(m);
            }
        });
    }

    private void appendData() {

        new Thread(new Runnable() {

            public void run() {
                while (true) {
                    stream.stream(data.get(r.nextInt(data.size())));
                    try {
                        Thread.sleep(r.nextInt(7) * 1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }).start();
    }

    public static void main(String[] args) {
        beforeClass();
        Engine e = new Engine();
        e.Test();
    }

    private List<Triple> readFile(String filename) {
        List<Triple> triples = null;
        Triple t;
        try {
            FileReader file = new FileReader(filename);
            Scanner input = new Scanner(file);           
            triples = new ArrayList<Triple>();
            while ((t = readTriple(input)) != null) {
                triples.add(t);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return triples;
    }

    private void print(Mapping m) {
        List<Node> list = toNodeList(m);
        for (Node n : list) {
            System.out.println(n.toString());
        }
    }

    private Triple readTriple(Scanner sc) {
        Triple t;
        String o, s, p;
        if (sc.hasNextLine()) {
            o = sc.nextLine();
        } else {
            return null;
        }
        if (sc.hasNextLine()) {
            s = sc.nextLine();
        } else {
            return null;
        }
        if (sc.hasNextLine()) {
            p = sc.nextLine();
        } else {
            return null;
        }
        t = new Triple(Node.createURI(o), Node.createURI(s), Node.createLiteral(p));
        return t;
    }

    private void writeToFile(List<Mapping> list) {
        try {
            FileWriter writer = new FileWriter("result.txt");
            for (Mapping m : list) {
                for (Node n : toNodeList(m)) {
                    writer.write(n.toString() + "\n");
                }
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static List<String> readRequests(String path) {
        List<String> list = null;
        Scanner input;
        try {
            input = new Scanner(new File(path));
            list = new ArrayList<String>();
            String str = "";
            while (input.hasNextLine()) {
                str += input.nextLine() + "\n";
            }
            input.close();
            String[] split = str.split("\n\n");
            for(String s : split)
                list.add(s);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }

        return list;
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

    private List<Triple> readTriples(final String path) {
        List<Triple> triples = new ArrayList<Triple>();

        Model model = ModelFactory.createDefaultModel();
        model.read(this.getClass().getResourceAsStream(path), null, "N-TRIPLE");

        List<Statement> statements = model.listStatements().toList();
        for (Statement s : statements) {
            triples.add(s.asTriple());
        }

        return triples;
    }

    private class DefaultRDFStream extends RDFStream {

        public DefaultRDFStream(ExecContext context, String uri) {
            super(context, uri);
        }

        public void stream(Model t) {
            StmtIterator iter = t.listStatements();
            while (iter.hasNext()) {
                super.stream(iter.next().asTriple());
            }
        }

        @Override
        public void stop() {
        }

    }
}
