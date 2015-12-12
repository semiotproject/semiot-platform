package ru.semiot.platform.apigateway.utils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import java.io.StringReader;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDFBase;

public class RDFMatcher {

    public static boolean match(String turtle, Node subject, Node predicate, Node object) {
        MatchSinkRDF matcher = new MatchSinkRDF(subject, predicate, object);
        RDFDataMgr.parse(matcher,
                new StringReader(turtle), Lang.TURTLE);

        return matcher.match;
    }

    private static class MatchSinkRDF extends StreamRDFBase {

        private final Node subject;
        private final Node predicate;
        private final Node object;
        private boolean match = false;

        public MatchSinkRDF(Node subject, Node predicate, Node object) {
            this.subject = subject;
            this.predicate = predicate;
            this.object = object;
        }

        @Override
        public void triple(Triple triple) {
            boolean subjectMatch = subject != Node.ANY ? 
                    triple.subjectMatches(subject) : true;
            boolean predicateMatch = predicate != Node.ANY ? 
                    triple.predicateMatches(predicate) : true;
            boolean objectMatch = object != Node.ANY ? 
                    triple.objectMatches(object) : true;
            
            if(subjectMatch && predicateMatch && objectMatch) {
                match = true;
            }
        }

        public boolean matches() {
            return match;
        }

    }

}
