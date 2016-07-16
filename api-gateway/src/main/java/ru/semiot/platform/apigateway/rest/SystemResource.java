package ru.semiot.platform.apigateway.rest;

import static ru.semiot.commons.restapi.AsyncResponseHelper.resume;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.HydraFilter;
import ru.semiot.commons.namespaces.Proto;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.commons.rdf.ModelJsonLdUtils;
import ru.semiot.commons.restapi.MediaType;
import ru.semiot.platform.apigateway.ServerConfig;
import ru.semiot.platform.apigateway.beans.TSDBQueryService;
import ru.semiot.platform.apigateway.beans.impl.ContextProvider;
import ru.semiot.platform.apigateway.beans.impl.SPARQLQueryService;
import ru.semiot.platform.apigateway.utils.MapBuilder;
import ru.semiot.platform.apigateway.utils.URIUtils;
import rx.Observable;
import rx.exceptions.Exceptions;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

@Path("/systems")
@Stateless
public class SystemResource extends AbstractSystemResource {

  private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);
  private static final Logger logger = LoggerFactory.getLogger(SystemResource.class);
  private static final String QUERY_GET_ALL_SYSTEMS =
      "SELECT ?uri ?id ?label ?prototype {"
          + " ?uri a ssn:System, proto:Individual ;"
          + "     dcterms:identifier ?id ;"
          + "     proto:hasPrototype ?prototype ."
          + " OPTIONAL { ?uri rdfs:label ?label }"
          + " FILTER NOT EXISTS { [] ssn:hasSubSystem ?uri }"
          + "} "
          + "ORDER BY ?id "
          + "LIMIT ${LIMIT} "
          + "OFFSET ${OFFSET} ";
  private static final String COUNT_TOTAL_SYSTEMS =
      "SELECT (COUNT(*) AS ?count) {"
          + "?system a ssn:System, proto:Individual ."
          + "FILTER NOT EXISTS { [] ssn:hasSubSystem ?system }"
          + "}";
  private static final String QUERY_DESCRIBE_SYSTEM = "CONSTRUCT {"
      + "  ?system ?p ?o ."
      + "  ?o ?o_p ?o_o ."
      + "} WHERE {"
      + "  ?system ?p ?o ;"
      + "    dcterms:identifier \"${SYSTEM_ID}\" ."
      + "  OPTIONAL {"
      + "    ?o ?o_p ?o_o ."
      + "    FILTER(?p NOT IN (rdf:type, proto:hasPrototype))"
      + "  }"
      + "}";

  private static final String VAR_URI = "uri";
  private static final String VAR_ID = "id";
  private static final String VAR_LABEL = "label";
  private static final String VAR_PROTOTYPE = "prototype";
  private static final String VAR_LIMIT = "${LIMIT}";
  private static final String VAR_OFFSET = "${OFFSET}";
  private static final String VAR_PAGE_SIZE = "${PAGE_SIZE}";
  private static final int FIRST_PAGE = 1;

  public SystemResource() {
    super();
  }

  @Inject
  private SPARQLQueryService sparqlQuery;
  @Inject
  private TSDBQueryService tsdbQuery;
  @Inject
  private ContextProvider contextProvider;
  @Context
  private UriInfo uriInfo;

  @GET
  @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
  public void listSystems(@Suspended final AsyncResponse response, @QueryParam("page") Integer page,
      @QueryParam("size") Integer size)
      throws JsonLdError, IOException {
    URI root = uriInfo.getRequestUri();
    if (page == null) {
      URI redirectUri = UriBuilder.fromUri(root)
          .queryParam("page", FIRST_PAGE).queryParam("size", config.systemsPageSize())
          .build();
      response.resume(Response.seeOther(redirectUri).build());
    } else {
      final int pageSize = size != null && size != 0 ? size : config.systemsPageSize();

      final Model model = contextProvider.getRDFModel(ContextProvider.SYSTEM_COLLECTION,
          MapBuilder.newMap()
              .put(ContextProvider.VAR_ROOT_URL, URIUtils.extractRootURL(root))
              .put(VAR_PAGE_SIZE, pageSize)
              .put(ContextProvider.VAR_QUERY_PARAMS, "?page=" + page + "&size=" + pageSize)
              .build());
      final Map<String, Object> frame = contextProvider.getFrame(
          ContextProvider.SYSTEM_COLLECTION, root);
      int offset = page > 1 ? (page - 1) * pageSize : 0;

      Resource collection = model.listResourcesWithProperty(
          RDF.type, Hydra.PartialCollectionView).next();
      Resource parentCollection = model.listObjectsOfProperty(collection, HydraFilter.viewOf)
          .next().asResource();

      Observable<Integer> totalItems = sparqlQuery.select(COUNT_TOTAL_SYSTEMS)
          .map((ResultSet rs) -> {
            if (rs.hasNext()) {
              int total = rs.next().getLiteral("count").getInt();
              model.add(parentCollection, Hydra.totalItems, ResourceFactory
                  .createTypedLiteral(String.valueOf(total), XSDDatatype.XSDinteger));

              if ((page + 1) * pageSize <= total) {
                model.add(collection, Hydra.next,
                    ResourceFactory.createResource(UriBuilder.fromUri(root)
                        .replaceQueryParam("page", page + 1)
                        .replaceQueryParam("size", pageSize).build()
                        .toASCIIString()));
              }

              model.add(collection, Hydra.last,
                  ResourceFactory.createResource(UriBuilder.fromUri(root)
                      .replaceQueryParam("page", computeLastPage(total, pageSize))
                      .replaceQueryParam("size", pageSize)
                      .build().toASCIIString()));
              return total;
            } else {
              logger.error("Can't count number of existing systems!");
              return null;
            }
          });

      Observable<ResultSet> systems = sparqlQuery.select(QUERY_GET_ALL_SYSTEMS
          .replace(VAR_LIMIT, String.valueOf(pageSize))
          .replace(VAR_OFFSET, String.valueOf(offset)));

      Observable.zip(systems, totalItems, (ResultSet rs, Integer total) -> {
        while (rs.hasNext()) {
          QuerySolution qs = rs.next();
          Resource system = qs.getResource(VAR_URI);
          Literal systemId = qs.getLiteral(VAR_ID);
          Literal systemLabel = qs.getLiteral(VAR_LABEL);
          Resource prototype = qs.getResource(VAR_PROTOTYPE);

          model.add(collection, Hydra.member, system);
          model.add(system, DCTerms.identifier, systemId);
          model.add(system, RDF.type, ResourceUtils.createResourceFromClass(root,
              prototype.getLocalName()));
          if (systemLabel != null) {
            model.add(system, RDFS.label, systemLabel);
          }
        }

        try {
          return JsonUtils.toPrettyString(ModelJsonLdUtils.toJsonLdCompact(model, frame));
        } catch (IOException | JsonLdError e) {
          throw Exceptions.propagate(e);
        }
      }).subscribe(resume(response));
    }
  }

  @GET
  @Path("{id}")
  @Produces({MediaType.APPLICATION_LD_JSON, MediaType.APPLICATION_JSON})
  public void getSystem(@Suspended final AsyncResponse response, @PathParam("id") String id)
      throws URISyntaxException, IOException {
    URI root = uriInfo.getRequestUri();
    String rootUrl = URIUtils.extractRootURL(root);
    Model model = contextProvider.getRDFModel(ContextProvider.SYSTEM_SINGLE,
        MapBuilder.newMap()
            .put(ContextProvider.VAR_ROOT_URL, rootUrl)
            .put(ContextProvider.VAR_SYSTEM_ID, id).build());

    sparqlQuery.describe(QUERY_DESCRIBE_SYSTEM.replace("${SYSTEM_ID}", id)).map((Model result) -> {
      model.add(result);
      try {
        Literal systemId = ResourceFactory.createTypedLiteral(id, XSDDatatype.XSDstring);
        boolean found = model.contains(null, DCTerms.identifier, systemId);
        if (found) {
          Resource system = model.listResourcesWithProperty(DCTerms.identifier, systemId).next();
          Resource prototype = model.listObjectsOfProperty(system, Proto.hasPrototype)
              .next().asResource();
          Resource prototypeResource = ResourceUtils.createResourceFromClass(
              root, prototype.getLocalName());
          model.add(system, RDF.type, prototypeResource);

          if (model.contains(null, RDF.type, SSN.SensingDevice)) {
            model.add(system,
                ResourceFactory.createProperty(rootUrl + "/doc#observations"),
                ResourceFactory.createResource(rootUrl + "/systems/" + systemId + "/observations"));
          }

          Map<String, Object> frame = contextProvider.getFrame(ContextProvider.SYSTEM_SINGLE,
              MapBuilder.newMap()
                  .put(ContextProvider.VAR_ROOT_URL, rootUrl)
                  .put(ContextProvider.VAR_SYSTEM_TYPE, prototypeResource.getURI())
                  .build());
          return JsonUtils.toPrettyString(ModelJsonLdUtils.toJsonLdCompact(model, frame));
        } else {
          throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      } catch (Exception ex) {
        throw Exceptions.propagate(ex);
      }
    }).subscribe(resume(response));
  }

  private int computeLastPage(int total, int pageSize) {
    if (total > 0) {
      Double value = Double.valueOf(total / pageSize);
      return Double.compare(value.intValue(), value) == 0 ? value.intValue() + 1 : value.intValue();
    } else {
      return FIRST_PAGE;
    }
  }
}
