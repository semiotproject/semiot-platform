package ru.semiot.platform.apigateway;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.commons.namespaces.NamespaceUtils;
import ru.semiot.commons.namespaces.Proto;
import ru.semiot.commons.namespaces.SHACL;
import ru.semiot.commons.namespaces.SSN;
import ru.semiot.commons.namespaces.SSNCOM;
import rx.Observable;
import rx.schedulers.Schedulers;

@Singleton
public class SPARQLQueryService {

    @Resource
    ManagedExecutorService mes;

    private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);
    private static final String PREFIXES = NamespaceUtils.toSPARQLPrologue(
            SSN.class, RDF.class, OWL.class, RDFS.class, SSNCOM.class, 
            DCTerms.class, XSD.class, Proto.class, Hydra.class, SHACL.class);

    private final HttpAuthenticator httpAuthenticator;

    public SPARQLQueryService() {
        this.httpAuthenticator = new SimpleAuthenticator(config.sparqlUsername(),
                config.sparqlPassword().toCharArray());
    }

    public Observable<ResultSet> select(String query) {
        return Observable.create(o -> {
            try {
                Query select = QueryFactory.create(PREFIXES + query);
                ResultSet rs = QueryExecutionFactory.createServiceRequest(
                        config.sparqlEndpoint(), select, httpAuthenticator).execSelect();
                o.onNext(rs);
            } catch (Throwable ex) {
                o.onError(ex);
            }

            o.onCompleted();
        }).subscribeOn(Schedulers.from(mes)).cast(ResultSet.class);
    }
    
    public ResultSet select(Model model, String query) {
        Query select = QueryFactory.create(PREFIXES + query);
        ResultSet rs = QueryExecutionFactory.create(select, model).execSelect();
        
        return rs;
    }

    public Observable<Model> describe(String query) {
        return Observable.create(o -> {
            Query describe = QueryFactory.create(PREFIXES + query);
            Model model = QueryExecutionFactory.createServiceRequest(
                    config.sparqlEndpoint(), describe, httpAuthenticator).execDescribe();

            o.onNext(model);
            o.onCompleted();
        }).subscribeOn(Schedulers.from(mes)).cast(Model.class);
    }

}
