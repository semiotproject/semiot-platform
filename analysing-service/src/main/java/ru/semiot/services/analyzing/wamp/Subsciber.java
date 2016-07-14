package ru.semiot.services.analyzing.wamp;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.services.analyzing.ServiceConfig;
import rx.Subscription;

@ApplicationScoped
@Named
public class Subsciber {

    private static final Logger logger = LoggerFactory
            .getLogger(Subsciber.class);
    private final WAMPClient wampClient = WAMPClient.getInstance();
    private Map<String, Subsc> listTopics;
    @Inject
    TopicListener topicListener;
    private static final Query TOPICS_QUERY = QueryFactory
            .create(new StringBuilder()
                    .append("prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> ")
                    .append("prefix ssncom: <http://purl.org/NET/ssnext/communication#> ")
                    .append("SELECT ?q where{ ?x ssncom:hasCommunicationEndpoint [ssncom:protocol \"WAMP\"; ssncom:topic ?q ]}").toString());

    public Subsciber() {
        connectWithDataStore();
        try {
            wampClient.init();
            listTopics = new HashMap<>();
        } catch (Exception ex) {
            logger.error("Something went wrong! " + ex.getMessage());
        }
    }

    public void subscribeTopics(List<String> topics, int query_id) {
        for (String topic : topics) {
            if (listTopics.containsKey(topic)) {
                if (!listTopics.get(topic).ids.contains(query_id)) {
                    listTopics.get(topic).ids.add(query_id);
                }
            } else {
                listTopics.put(topic, new Subsc(wampClient.subscribe(topic).subscribe(topicListener), query_id));
            }
        }
    }

    public void unsubscribeTopics(List<String> topics, int query_id) {
        for (String topic : topics) {
            if (listTopics.containsKey(topic)) {
                if (listTopics.get(topic).ids.contains(query_id)) {
                    if (listTopics.get(topic).ids.size() == 1) {
                        listTopics.get(topic).subscription.unsubscribe();
                        listTopics.remove(topic);
                    } else {                        
                        listTopics.get(topic).ids.remove((Integer) query_id);
                    }
                }
            }
        }
    }

    private void connectWithDataStore() {
        try {
            URI uri = new URI(ServiceConfig.config.storeUrl());
            String URL = "http://" + uri.getHost() + ":" + uri.getPort();
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(URL);
            HttpResponse resp = null;

            while (true) {
                try {
                    resp = client.execute(request);
                    if (resp.getStatusLine().getStatusCode() == 200) {
                        logger.info("Connected to " + URL);
                        break;
                    }
                } catch (HttpHostConnectException ex) {
                    logger.info("Try to connect with " + URL + "after 2s");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex1) {
                        logger.error("Something went wrong with error:\n" + ex1.getMessage());
                    }
                } catch (IOException ex) {
                    logger.error("Something went wrong with error:\n" + ex.getMessage());
                }
            }
        } catch (URISyntaxException ex) {
            logger.error("The storeURL is WRONG!!!");
        }
    }

    private static class Subsc {

        Subscription subscription;
        List<Integer> ids;

        public Subsc(Subscription subsc, int id) {
            subscription = subsc;
            ids = new ArrayList<>();
            ids.add(id);
        }

    }
}
