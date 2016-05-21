package ru.semiot.platform.apigateway.rest;

import static ru.semiot.commons.restapi.AsyncResponseHelper.resume;
import static ru.semiot.platform.apigateway.beans.impl.ContextProvider.API_DOCUMENTATION;
import static ru.semiot.platform.apigateway.beans.impl.ContextProvider.ENTRYPOINT;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.Proto;
import ru.semiot.commons.namespaces.SEMIOT;
import ru.semiot.commons.namespaces.SHACL;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.commons.rdf.ModelJsonLdUtils;
import ru.semiot.commons.restapi.MediaType;
import ru.semiot.platform.apigateway.ServerConfig;
import ru.semiot.platform.apigateway.beans.impl.ContextProvider;
import ru.semiot.platform.apigateway.beans.impl.SPARQLQueryService;
import ru.semiot.platform.apigateway.utils.Credentials;
import ru.semiot.platform.apigateway.utils.DataBase;
import ru.semiot.platform.apigateway.utils.MapBuilder;
import ru.semiot.platform.apigateway.utils.URIUtils;
import rx.Observable;
import rx.exceptions.Exceptions;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/")
@Stateless
public class RootResource {

  private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);
  private static final Logger logger = LoggerFactory.getLogger(RootResource.class);
  private static final String QUERY_SYSTEM_PROTOTYPES
      = "SELECT DISTINCT ?prototype {"
      + "	?device a proto:Individual, ssn:System ;"
      + "    	proto:hasPrototype ?prototype ."
      + "}";
  private static final String QUERY_COLLECTION_MEMBER
      = "SELECT ?uri {"
      + " <${COLLECTION_URI}> rdfs:range ?shape ."
      + " ?shape sh:property ?uri ."
      + " ?uri sh:predicate hydra:member ."
      + "}";
  private static final String QUERY_INDIVIDUAL_PROPERTIES
      = "SELECT DISTINCT ?prototype ?property {"
      + " ?device a proto:Individual ;"
      + "     proto:hasPrototype <${PROTOTYPE_URI}> ;"
      + "     proto:hasPrototype ?prototype ;"
      + "     ?property ?value ."
      + " FILTER(isLiteral(?value))"
      + "}";
  private static final String QUERY_DESCRIBE_SUPPORTED_PROCESSES
      = "DESCRIBE ?process {"
      + " ?device a proto:Individual ;"
      + "   proto:hasPrototype <${PROTOTYPE_URI}> ;"
      + "   semiot:supportedProcess ?process ."
      + "}";
  private static final String QUERY_DESCRIBE_COMMANDS
      = "DESCRIBE ?command {"
      + " <${PROCESS_URI}> proto:hasPrototype ?prototype ."
      + " ?prototype semiot:supportedCommand ?command ."
      + "}";
  private static final String QUERY_PROPERTIES_AND_UNITS
      = "SELECT ?propertyOrUnit ?label {"
      + " VALUES ?prototype { ${PROTOTYPES} }"
      + " {"
      + "   ?prototype ssn:hasSubSystem/ssn:observes ?propertyOrUnit ."
      + " } UNION {"
      + "   ?prototype ssn:hasSubSystem/ssn:hasMeasurementCapability/ssn:hasMeasurementProperty ?p ."
      + "   ?p a qudt:Unit ;"
      + "      ssn:hasValue/ssn:hasValue ?propertyOrUnit ."
      + " }"
      + " ?propertyOrUnit rdfs:label ?label ."
      + "}";
  private static final String LINK_SYSTEMS = "systems";
  private static final String VAR_PROTOTYPE = "prototype";
  private static final String VAR_PROPERTY = "property";
  private static final String VAR_PROCESS = "process";
  private static final String VAR_URI = "uri";
  private static final String VAR_PROPERTY_OR_UNIT = "propertyOrUnit";
  private static final String VAR_LABEL = "label";
  private static final String VAR_COLLECTION_URI = "${COLLECTION_URI}";
  private static final String VAR_PROTOTYPE_URI = "${PROTOTYPE_URI}";
  private static final String VAR_PROCESS_URI = "${PROCESS_URI}";
  private static final String VAR_PROTOTYPES = "${PROTOTYPES}";

  public RootResource() {
  }

  @Inject
  private SPARQLQueryService query;

  @Context
  private UriInfo uriInfo;

  @Inject
  private ContextProvider contextProvider;

  @Inject
  private DataBase db;

  @GET
  @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
  public String entrypoint() throws JsonLdError, IOException, URISyntaxException {
    URI root = uriInfo.getRequestUri();
    Model entrypoint = contextProvider.getRDFModel(ENTRYPOINT, root);
    Map<String, Object> frame = contextProvider.getFrame(ENTRYPOINT, root);

    Object entrypointObj = ModelJsonLdUtils.toJsonLd(entrypoint);

    return JsonUtils.toString(JsonLdProcessor.frame(entrypointObj, frame, new JsonLdOptions()));
  }

  @GET
  @Produces({MediaType.TEXT_HTML, MediaType.TEXT_PLAIN})
  public Response index() {
    return Response.seeOther(URI.create("/index")).build();
  }

  @GET
  @Path("/context")
  public String context() {
    URI root = uriInfo.getRequestUri();
    return contextProvider.getContext(ContextProvider.COMMON_CONTEXT, root);
  }

  @GET
  @Path("/doc")
  @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
  public void documentation(@Suspended final AsyncResponse response)
      throws JsonLdError, IOException {
    String rootURL = URIUtils.extractRootURL(uriInfo.getRequestUri());
    Model apiDoc = contextProvider.getRDFModel(API_DOCUMENTATION, MapBuilder.newMap()
        .put(ContextProvider.VAR_ROOT_URL, rootURL)
        .put(ContextProvider.VAR_WAMP_URL, rootURL + config.wampPublicPath())
        .build());
    Map<String, Object> frame = contextProvider.getFrame(API_DOCUMENTATION, rootURL);

    Observable<List<Resource>> prototypes = query.select(QUERY_SYSTEM_PROTOTYPES)
        .map((ResultSet rs) ->
            defineResourceIndividual(apiDoc, rootURL, LINK_SYSTEMS, rs, SSN.System));

    Observable<Model> supportedProperties = addSupportedProperties(apiDoc, rootURL, prototypes);
    Observable<Model> supportedProcesses = addSupportedProcesses(apiDoc, rootURL, prototypes);
//    Observable<Model> propertiesAndUnits = addPropertiesAndUnits(apiDoc, prototypes);

    Observable.zip(supportedProperties, supportedProcesses, (a, b) -> {
      try {
        return JsonUtils.toString(ModelJsonLdUtils.toJsonLdCompact(apiDoc, frame));
      } catch (Throwable e) {
        throw Exceptions.propagate(e);
      }
    }).subscribe(resume(response));
  }

  private List<Resource> defineResourceIndividual(Model model, String rootURL,
      String collectionName, ResultSet rs, Resource... classes) {
    List<Resource> resultPrototypes = new ArrayList<>();
    final Resource apiDocResource = model.listResourcesWithProperty(
        RDF.type, Hydra.ApiDocumentation).next();
    final Resource collection = ResourceFactory.createResource(rootURL + "/doc#" + collectionName);

    //Find the restriction on hydra:member of the given collection
    ResultSet results = query.select(model, QUERY_COLLECTION_MEMBER
        .replace(VAR_COLLECTION_URI, collection.getURI()));
    Resource restriction = null;
    if (results.hasNext()) {
      restriction = results.next().getResource(VAR_URI);
    }

    while (rs.hasNext()) {
      final Resource prototype = rs.next().getResource(VAR_PROTOTYPE);
      resultPrototypes.add(prototype);
      final Resource prototypeResource = ResourceUtils.createResourceFromClass(
          rootURL, prototype.getLocalName());

      //Define in hydra:supportedClass
      model.add(apiDocResource, Hydra.supportedClass, prototypeResource);
      model.add(prototypeResource, RDF.type, Hydra.Class);
      model.add(prototypeResource, RDF.type, Proto.Individual);
      for (Resource clazz : classes) {
        model.add(prototypeResource, RDF.type, clazz);
      }
      model.add(prototypeResource, Proto.hasPrototype, prototype);

      //Define in the given collection
      if (restriction != null) {
        model.add(restriction, SHACL.clazz, prototypeResource);
      }
    }

    return resultPrototypes;
  }

  private Observable<Model> addSupportedProperties(Model model, String rootUrl,
      Observable<List<Resource>> observable) {
    return observable.map((prototypes) -> {
      List<Observable<ResultSet>> obs = new ArrayList<>();
      prototypes.stream().forEach((prototype) -> obs.add(query.select(
          QUERY_INDIVIDUAL_PROPERTIES.replace(VAR_PROTOTYPE_URI, prototype.getURI()))));

      return Observable.merge(obs).toBlocking().toIterable();
    }).map((Iterable<ResultSet> iter) -> {
      iter.forEach((ResultSet rs) -> {
        while (rs.hasNext()) {
          QuerySolution qs = rs.next();
          Resource prototype = qs.getResource(VAR_PROTOTYPE);
          Resource prototypeResource = ResourceUtils.createResourceFromClass(
              rootUrl, prototype.getLocalName());
          Resource property = ResourceFactory.createResource();
          model.add(prototypeResource, Hydra.supportedProperty, property)
              .add(property, Hydra.property,
                  ResourceFactory.createProperty(qs.getResource(VAR_PROPERTY).getURI()));
        }
      });
      return model;
    });
  }

  private Observable<Model> addSupportedProcesses(Model model, String rootUrl,
      Observable<List<Resource>> observable) {
    return observable.map((prototypes) -> {
      List<Observable<Map.Entry<Object, Model>>> obs = new ArrayList<>();
      prototypes.stream().forEach((prototype) -> obs.add(query.describe(
          ResourceUtils.createResourceFromClass(rootUrl, prototype.getLocalName()),
          QUERY_DESCRIBE_SUPPORTED_PROCESSES.replace(VAR_PROTOTYPE_URI, prototype.getURI()))));

      return Observable.merge(obs).toBlocking().toIterable();
    }).map((Iterable<Map.Entry<Object, Model>> iter) -> {
      List<Observable<Map.Entry<Object, Model>>> obs = new ArrayList<>();
      iter.forEach((Map.Entry<Object, Model> rs) -> {
        Resource prototypeResource = (Resource) rs.getKey();
        Model value = rs.getValue();
        if (!value.isEmpty()) {
          Resource process = value.listSubjectsWithProperty(Proto.hasPrototype).next();
          value.add(prototypeResource, SEMIOT.supportedProcess, process);

          model.add(value);

          obs.add(query.describe(
              process, QUERY_DESCRIBE_COMMANDS.replace(VAR_PROCESS_URI, process.getURI())));
        }
      });
      return Observable.merge(obs).toBlocking().toIterable();
    }).map((Iterable<Map.Entry<Object, Model>> iter) -> {
      iter.forEach((Map.Entry<Object, Model> rs) -> {
        Resource process = (Resource) rs.getKey();
        Model value = rs.getValue();
        Resource operation = ResourceFactory.createResource();
        value.add(process, Hydra.supportedOperation, operation)
            .add(operation, RDF.type, Hydra.Operation)
            .add(operation, Hydra.method, "POST")
            .add(operation, Hydra.returns, SEMIOT.CommandResult);

        ResIterator commandIter = value.listSubjectsWithProperty(RDF.type, SEMIOT.Command);
        while (commandIter.hasNext()) {
          Resource command = commandIter.next();
          value.add(operation, Hydra.expects, command);
        }

        model.add(value);
      });
      return model;
    });
  }

  private Observable<Model> addPropertiesAndUnits(Model model,
      Observable<List<Resource>> observable) {
    return observable
        .map((List<Resource> ps) -> StringUtils.join(
            ps.stream().map((p) -> "<" + p.getURI() + ">").toArray(String[]::new), " "))
        .map((values) -> query
            .select(QUERY_PROPERTIES_AND_UNITS.replace(VAR_PROTOTYPES, values))
            .toBlocking().toIterable())
        .map((Iterable<ResultSet> iter) -> {
          iter.forEach((rs) -> {
            Resource doc = model.listSubjectsWithProperty(RDF.type, Hydra.ApiDocumentation).next();

            while (rs.hasNext()) {
              QuerySolution qs = rs.next();
              Resource propertyOrUnit = qs.getResource(VAR_PROPERTY_OR_UNIT);
              Literal label = qs.getLiteral(VAR_LABEL);

              model.add(doc, Hydra.supportedClass, propertyOrUnit)
                  .add(propertyOrUnit, RDF.type, Hydra.Class)
                  .add(propertyOrUnit, RDFS.label, label);
            }
          });
          return model;
        });
  }

  @GET
  @Path("/logout")
  //TODO: Move somewhere else
  public void getContext(@Context HttpServletRequest req, @Context HttpServletResponse resp)
      throws Exception {
    resp.setHeader("Cache-Control", "no-cache, no-store");
    resp.setHeader("Pragma", "no-cache");
    resp.setHeader("Expires", new java.util.Date().toString());
    if (req.getSession(false) != null) {
      req.getSession(false).invalidate();// remove session.
    }
    req.logout();
    resp.sendRedirect("/");
  }

  @GET
  @Path("/user")
  @Produces(MediaType.APPLICATION_JSON)
  //TODO: Move somewhere else
  public void getUserData(@Context HttpServletRequest req, @Context HttpServletResponse resp)
      throws Exception {
    Credentials c = db.getUser(req.getRemoteUser());
    if (c != null) {
      resp.getWriter().write(
          "{\"username\": \"" + c.getLogin() + "\", \"password\": \"" + c.getPassword() + "\"}");
      resp.getWriter().flush();
      resp.getWriter().close();
    } else {
      resp.sendError(401);//Forbidden
    }
  }
}
