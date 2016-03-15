package ru.semiot.platform.apigateway.rest;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;
import rx.functions.Action1;

public class ResourceHelper {

    private static final Logger logger = LoggerFactory.getLogger(ResourceHelper.class);

    public static <T> Subscriber<T> resume(AsyncResponse response) {
        return new ResourceHelper.Resume(response);
    }
    
    public static Action1<Throwable> resumeOnError(AsyncResponse response) {
        return new ResourceHelper.ActionOnError(response);
    }
    
    private static class ActionOnError implements Action1<Throwable> {
        
        private final AsyncResponse response;
        
        public ActionOnError(AsyncResponse response) {
            this.response = response;
        }

        @Override
        public void call(Throwable e) {
            logger.warn(e.getMessage(), e);
            
            response.resume(e);
        }
        
    }

    private static class Resume<T> extends Subscriber<T> {

        private final AsyncResponse response;
        private final Status fallbackStatus;
        private boolean calledOnNext = false;

        public Resume(AsyncResponse response) {
            this.response = response;
            this.fallbackStatus = null;
        }

        public Resume(AsyncResponse response, Status fallbackStatus) {
            this.response = response;
            this.fallbackStatus = fallbackStatus;
        }

        @Override
        public void onCompleted() {
            if (!calledOnNext) {
                if (fallbackStatus != null) {
                    response.resume(fallbackStatus);
                } else {
                    response.resume(Response.status(Response.Status.NO_CONTENT));
                }
            }
        }

        @Override
        public void onError(Throwable e) {
            logger.warn(e.getMessage(), e);

            response.resume(e);
        }

        @Override
        public void onNext(T t) {
            calledOnNext = true;

            response.resume(t);
        }

    }

}
