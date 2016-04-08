package ru.semiot.services.tsdbservice.wamp;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.DUL;
import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.SEMIOT;
import ru.semiot.services.tsdbservice.TSDBClient;
import ru.semiot.services.tsdbservice.model.Command;

public class CommandListener extends RDFMessageObserver {

    private static final Logger logger = LoggerFactory
            .getLogger(CommandListener.class);
    private static final Query GET_INFORMATION = QueryFactory.create(
            NamespaceUtils.newSPARQLQuery(
                    "SELECT ?uri ?datetime ?type {" +
                            "?command a ?type ; " +
                            "dul:hasEventTime ?datetime ;" +
                            "dul:involvesAgent ?uri ." +
                            "FILTER(?type != semiot:Command)" +
                            "} LIMIT 1", DUL.class, SEMIOT.class));
    private static final Query GET_PROPERTIES = QueryFactory.create(
            NamespaceUtils.newSPARQLQuery(
                    "SELECT ?uri ?value {" +
                            "?command a semiot:Command ;" +
                            "  ?uri ?value ." +
                            "FILTER(?uri != rdf:type " +
                            "  && ?uri != dul:hasEventTime " +
                            "  && ?uri != dul:involvesAgent)" +
                            "}", SEMIOT.class, DUL.class, RDF.class));

    @Override
    public void onNext(Model model) {
        try {
            if (isCommand(model)) {
                ResultSet rsCommands = query(model, GET_INFORMATION);
                if (rsCommands.hasNext()) {
                    QuerySolution qsCommands = rsCommands.next();

                    Resource deviceUri = qsCommands.getResource("uri");
                    Literal dateTime = qsCommands.getLiteral("datetime");
                    Resource type = qsCommands.getResource("type");

                    ResultSet rsProps = query(model, GET_PROPERTIES);

                    Command command = new Command(
                            NamespaceUtils.extractLocalName(deviceUri.getURI()),
                            dateTime.getLexicalForm(),
                            type.getURI());

                    while (rsProps.hasNext()) {
                        QuerySolution qsProps = rsProps.next();
                        command.addProperty(
                                qsProps.getResource("uri"),
                                qsProps.get("value"));
                    }

                    String query = command.toInsertCommandQuery();

                    TSDBClient.getInstance().executeAsync(query)
                            .subscribe(TSDBClient.onError());

                    logger.debug("Query: {}", query);
                } else {
                    logger.warn("Required properties not found!");
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void onError(Throwable e) {
        logger.error(e.getMessage(), e);
    }

    private boolean isCommand(Model model) {
        return !model.isEmpty() && model.contains(null, RDF.type, SEMIOT.Command);
    }
}
