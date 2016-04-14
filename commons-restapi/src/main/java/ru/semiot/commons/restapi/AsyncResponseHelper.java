package ru.semiot.commons.restapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;
import rx.functions.Action1;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

public class AsyncResponseHelper {

    private static final Logger logger = LoggerFactory.getLogger(AsyncResponseHelper.class);

    public static <T> Subscriber<T> resume(AsyncResponse response) {
        return new AsyncResponseHelper.Resume(response);
    }

    public static void resumeOnError(AsyncResponse response, Throwable e) {
        new AsyncResponseHelper.ActionOnError(response).call(e);
    }

    public static Action1<Throwable> resumeOnError(AsyncResponse response) {
        return new AsyncResponseHelper.ActionOnError(response);
    }

    public static void safeResume(AsyncResponse response, Throwable e) {
        if (!response.resume(e)) {
            logger.error("Response was not resumed!");
        }
    }

    private static void safeResume(AsyncResponse response, Object object) {
        if (!response.resume(object)) {
            logger.error("Response was not resumed!");
        }
    }

    private static class ActionOnError implements Action1<Throwable> {

        private final AsyncResponse response;

        public ActionOnError(AsyncResponse response) {
            this.response = response;
        }

        @Override
        public void call(Throwable e) {
            logger.warn(e.getMessage(), e);

            safeResume(response, e);
        }

    }

    private static class Resume<T> extends Subscriber<T> {

        private final AsyncResponse response;
        private final Response.Status fallbackStatus;
        private boolean calledOnNext = false;

        public Resume(AsyncResponse response) {
            this.response = response;
            this.fallbackStatus = null;
        }

        public Resume(AsyncResponse response, Response.Status fallbackStatus) {
            this.response = response;
            this.fallbackStatus = fallbackStatus;
        }

        @Override
        public void onCompleted() {
            if (!calledOnNext) {
                if (fallbackStatus != null) {
                    safeResume(response, Response.status(
                            fallbackStatus).build());
                } else {
                    safeResume(response, Response.status(
                            Response.Status.NO_CONTENT).build());
                }
            }
        }

        @Override
        public void onError(Throwable e) {
            logger.warn(e.getMessage(), e);

            safeResume(response, e);
        }

        @Override
        public void onNext(T t) {
            calledOnNext = true;

            safeResume(response, t);
        }

    }
}
