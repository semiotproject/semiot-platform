package ru.semiot.services.tsdbservice.wamp;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.DUL;
import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.SEMIOT;
import ru.semiot.services.tsdbservice.TSDBClient;
import ru.semiot.services.tsdbservice.model.CommandResult;

import java.io.StringWriter;

public class CommandResultListener extends RDFMessageObserver {

  private static final Logger logger = LoggerFactory.getLogger(CommandResultListener.class);
  private static final Query GET_INFORMATION = QueryFactory.create(NamespaceUtils.newSPARQLQuery(
      "SELECT ?uri ?datetime ?type ?process {" +
          "?actuation semiot:isResultOf ?command ;" +
          "semiot:commandResultTime ?datetime ;" +
          "dul:associatedWith ?uri ." +
          "?command a ?type ;" +
          " semiot:forProcess ?process ." +
          "FILTER(?type != semiot:Command)" +
          "} LIMIT 1", DUL.class, SEMIOT.class));
  private static final Query GET_PROPERTIES = QueryFactory.create(NamespaceUtils.newSPARQLQuery(
      "SELECT ?uri ?value {" +
          "?command semiot:forProcess ?process ;" +
          "  ?uri ?value ." +
          "FILTER(?uri != rdf:type && ?uri != dul:hasParameter && " +
          "   ?uri != semiot:forProcess && ?uri != dul:associatedWith)" +
          "}", SEMIOT.class, DUL.class, RDF.class));
  private static final Query GET_PARAMETERS = QueryFactory.create(NamespaceUtils.newSPARQLQuery(
      "SELECT ?uri ?value {" +
          "?command semiot:forProcess ?process ;" +
          " dul:hasParameter ?parameter ." +
          "?parameter semiot:forParameter ?uri ;" +
          " dul:hasParameterDataValue ?value ." +
          "}", DUL.class, SEMIOT.class));

  @Override
  public void onNext(Model model) {
    try {
      ResultSet rsCommandResults = query(model, GET_INFORMATION);
      if (rsCommandResults.hasNext()) {
        QuerySolution qsCommandResults = rsCommandResults.next();

        Resource deviceUri = qsCommandResults.getResource("uri");
        Resource processUri = qsCommandResults.getResource("process");
        Literal dateTime = qsCommandResults.getLiteral("datetime");
        Resource type = qsCommandResults.getResource("type");

        ResultSet rsProps = query(model, GET_PROPERTIES);

        logger.debug(dateTime.getLexicalForm());
        CommandResult commandResult = new CommandResult(
            NamespaceUtils.extractLocalName(deviceUri.getURI()),
            NamespaceUtils.extractLocalName(processUri.getURI()),
            dateTime.getLexicalForm(),
            type.getURI());

        while (rsProps.hasNext()) {
          QuerySolution qsProps = rsProps.next();
          commandResult.addProperty(ResourceFactory.createProperty(
              qsProps.getResource("uri").getURI()), qsProps.get("value"));
        }

        ResultSet rsParams = query(model, GET_PARAMETERS);

        while (rsParams.hasNext()) {
          QuerySolution qsParams = rsParams.next();
          commandResult.addParameter(qsParams.getResource("uri"), qsParams.getLiteral("value"));
        }

        String query = commandResult.toInsertQuery();
        logger.debug(query);
        TSDBClient.getInstance().executeAsync(query).subscribe(TSDBClient.onError());

        logger.debug("Query: {}", query);
      } else {
        logger.warn("Required properties not found!");
      }
    } catch (Throwable e) {
      logger.error(e.getMessage(), e);

      StringWriter writer = new StringWriter();
      model.write(writer, RDFLanguages.TURTLE.getName());
      logger.debug("Received command result: {}", writer.toString());
    }
  }

  @Override
  public void onError(Throwable e) {
    logger.error(e.getMessage(), e);
  }
}
