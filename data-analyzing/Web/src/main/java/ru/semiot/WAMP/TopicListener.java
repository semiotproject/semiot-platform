package ru.semiot.WAMP;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.StringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.semiot.cqels.Engine;
import rx.Observer;

public class TopicListener implements Observer<String> {

    private static final Logger logger = LoggerFactory
            .getLogger(TopicListener.class);
    private final String topicName;

    public TopicListener(String topicName) {
        this.topicName = topicName;
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        logger.warn(e.getMessage(), e);
    }

    @Override
    public void onNext(String message) {
        //Engine.writeFile(message);
        Model description = ModelFactory.createDefaultModel().read(
                new StringReader(message), null, SubscribeListener.LANG);
        if (!description.isEmpty()) {
            Engine.appendData(description);
        }
    }

}
