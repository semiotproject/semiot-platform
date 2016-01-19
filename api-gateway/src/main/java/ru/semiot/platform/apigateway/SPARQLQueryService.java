package ru.semiot.platform.apigateway;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import rx.Observable;
import rx.schedulers.Schedulers;

@Singleton
public class SPARQLQueryService {

    @Resource
    ManagedExecutorService mes;

    private static final ServerConfig config = ConfigFactory.create(ServerConfig.class);
    private static final String PREFIXES
            = "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>\n"
            + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX ssncom: <http://purl.org/NET/ssnext/communication#>\n"
            + "PREFIX dcterms: <http://purl.org/dc/terms/#>\n"
            + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
            + "PREFIX proto: <http://w3id.org/semiot/ontologies/proto#>\n";

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
