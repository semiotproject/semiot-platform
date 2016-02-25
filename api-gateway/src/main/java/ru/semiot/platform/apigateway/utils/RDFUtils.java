package ru.semiot.platform.apigateway.utils;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDFBase;

public class RDFUtils {

    private static final JsonLdOptions DEFAULT_OPTIONS = new JsonLdOptions();

    public static boolean match(String turtle, Node subject, Node predicate, Node object) {
        MatchSinkRDF matcher = new MatchSinkRDF(subject, predicate, object);
        RDFDataMgr.parse(matcher,
                new StringReader(turtle), Lang.TURTLE);

        return matcher.match;
    }

    public static Object toJsonLd(Model model) throws IOException {
        StringWriter writer = new StringWriter();
        model.write(writer, Lang.JSONLD.getName());

        return JsonUtils.fromString(writer.toString());
    }

    public static Object toJsonLdCompact(Model model, Object frame)
            throws JsonLdError, IOException {
        return JsonLdProcessor.compact(JsonLdProcessor.frame(
                RDFUtils.toJsonLd(model), frame, DEFAULT_OPTIONS),
                frame, DEFAULT_OPTIONS);
    }

    public static List<Resource> listResourcesWithProperty(Model model, Property p,
            RDFNode... objects) {
        Map<Resource, Integer> counts = new HashMap<>();
        for (RDFNode object : objects) {
            List<Resource> resources = model.listResourcesWithProperty(p, object)
                    .toList();
            resources.stream().forEach((resource) -> {
                if (counts.containsKey(resource)) {
                    counts.put(resource, counts.get(resource) + 1);
                } else {
                    counts.put(resource, 1);
                }
            });
        }

        return Arrays.asList(counts.keySet().stream().filter(
                (resource) -> (counts.get(resource) == objects.length)).toArray(Resource[]::new));
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
            boolean subjectMatch = subject != Node.ANY
                    ? triple.subjectMatches(subject) : true;
            boolean predicateMatch = predicate != Node.ANY
                    ? triple.predicateMatches(predicate) : true;
            boolean objectMatch = object != Node.ANY
                    ? triple.objectMatches(object) : true;

            if (subjectMatch && predicateMatch && objectMatch) {
                match = true;
            }
        }

        public boolean matches() {
            return match;
        }

    }

}
