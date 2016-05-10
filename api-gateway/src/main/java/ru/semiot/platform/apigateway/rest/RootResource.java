package ru.semiot.platform.apigateway.rest;

import static ru.semiot.commons.restapi.AsyncResponseHelper.resume;
import static ru.semiot.platform.apigateway.beans.impl.ContextProvider.API_DOCUMENTATION;
import static ru.semiot.platform.apigateway.beans.impl.ContextProvider.ENTRYPOINT;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.Proto;
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
import ru.semiot.platform.apigateway.utils.RDFUtils;
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
  private static final String QUERY_SENSOR_PROTOTYPES
      = "SELECT DISTINCT ?prototype {"
      + " ?device a proto:Individual, ssn:SensingDevice ;"
      + "         proto:hasPrototype ?prototype ."
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
      + " FILTER(?property NOT IN (rdf:type, proto:hasPrototype))"
      + "}";
  private static final String VAR_PROTOTYPE = "prototype";
  private static final String VAR_PROPERTY = "property";
  private static final String VAR_URI = "uri";
  private static final String VAR_COLLECTION_URI = "${COLLECTION_URI}";
  private static final String VAR_PROTOTYPE_URI = "${PROTOTYPE_URI}";

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
    Model apiDoc = contextProvider.getRDFModel(API_DOCUMENTATION,
        MapBuilder.newMap()
            .put(ContextProvider.VAR_ROOT_URL, rootURL)
            .put(ContextProvider.VAR_WAMP_URL, UriBuilder.fromUri(rootUrl + config.wampPublicPath()).scheme("ws"))
            .build());
    Map<String, Object> frame = contextProvider.getFrame(API_DOCUMENTATION, rootURL);

    Observable<List<Resource>> systems = query.select(QUERY_SYSTEM_PROTOTYPES)
        .map((ResultSet rs) -> defineResourceIndividual(
            apiDoc, rootURL, "EntryPoint-Systems", rs, SSN.System));
    Observable<List<Resource>> sensors = query.select(QUERY_SENSOR_PROTOTYPES)
        .map((ResultSet rs) -> defineResourceIndividual(
            apiDoc, rootURL, "EntryPoint-Sensors", rs, SSN.SensingDevice));

    Observable.zip(systems, sensors, (rsSystems, rsSensors) -> {
      List<Resource> rs = new ArrayList<>(rsSystems);
      rs.addAll(rsSensors);
      List<Observable<ResultSet>> obs = new ArrayList<>();
      rs.stream().forEach((prototype) -> obs.add(query.select(QUERY_INDIVIDUAL_PROPERTIES
          .replace(VAR_PROTOTYPE_URI, prototype.getURI()))));

      return Observable.merge(obs).toBlocking().toIterable();
    }).map((Iterable<ResultSet> iter) -> {
      iter.forEach((ResultSet rs) -> {
        while (rs.hasNext()) {
          QuerySolution qs = rs.next();
          Resource prototype = qs.getResource(VAR_PROTOTYPE);
          Resource prototypeResource = ResourceUtils.createResourceFromClass(
              rootURL, prototype.getLocalName());
          Property property = ResourceFactory.createProperty(
              qs.getResource(VAR_PROPERTY).getURI());

          apiDoc.add(prototypeResource, Hydra.supportedProperty, property);
        }
      });
      return apiDoc;
    }).lastOrDefault(apiDoc).map((__) -> {
      try {
        return JsonUtils.toString(ModelJsonLdUtils.toJsonLdCompact(apiDoc, frame));
      } catch (JsonLdError | IOException e) {
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

  @GET
  @Path("/logout")
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
  public void getUserData(@Context HttpServletRequest req, @Context HttpServletResponse resp)
      throws Exception {
    Credentials c = db.getUser(req.getRemoteUser());
    if (c != null) {
      resp.getWriter().write(
          "{\"username\": \"" + c.getLogin() + "\", \"password\": \"" + c.getPassword() + "\"}");
      resp.getWriter().flush();
      resp.getWriter().close();
    }
  }
}
