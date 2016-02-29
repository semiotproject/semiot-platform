package ru.semiot.platform.apigateway.rest;

import javax.ws.rs.container.AsyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

public class ResourceHelper {

    private static final Logger logger = LoggerFactory.getLogger(ResourceHelper.class);
    
    public static <T> Subscriber<T> resume(AsyncResponse response) {
        return new ResourceHelper.Resume(response);
    }
    
    private static class Resume<T> extends Subscriber<T> {

        private final AsyncResponse response;
        
        public Resume(AsyncResponse response) {
            this.response = response;
        }
        
        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            logger.warn(e.getMessage(), e);
            
            response.resume(e);
        }

        @Override
        public void onNext(T t) {
            response.resume(t);
        }
    
    }
    
}
