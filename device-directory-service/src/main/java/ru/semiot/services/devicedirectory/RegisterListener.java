package ru.semiot.services.devicedirectory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.StringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observer;

public class RegisterListener implements Observer<String> {
    
    private static final Logger logger = LoggerFactory.getLogger(
            RegisterListener.class);
    private final RDFStore store = RDFStore.getInstance();

    @Override
    public void onCompleted() {
        
    }

    @Override
    public void onError(Throwable e) {
        logger.warn(e.getMessage(), e);
    }

    @Override
    public void onNext(String t) {
        Model description = ModelFactory.createDefaultModel()
                .read(new StringReader(t), null, "TURTLE");
        store.save(description);
    }
}
