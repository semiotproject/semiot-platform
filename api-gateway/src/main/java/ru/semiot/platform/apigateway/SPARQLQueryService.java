package ru.semiot.platform.apigateway;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
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
            + "prefix owl: <http://www.w3.org/2002/07/owl#>\n"
            + "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "prefix ssncom: <http://purl.org/NET/ssnext/communication#>\n";

    private final HttpAuthenticator httpAuthenticator;

    public SPARQLQueryService() {
        this.httpAuthenticator = new SimpleAuthenticator(config.sparqlUsername(),
                config.sparqlPassword().toCharArray());
    }

    public Observable<ResultSet> select(String query) {
        return Observable.create(o -> {
            Query select = QueryFactory.create(PREFIXES + query);
            ResultSet rs = QueryExecutionFactory.createServiceRequest(
                    config.sparqlEndpoint(), select, httpAuthenticator).execSelect();
            o.onNext(rs);
            o.onCompleted();
        }).subscribeOn(Schedulers.from(mes)).cast(ResultSet.class);
    }

}
