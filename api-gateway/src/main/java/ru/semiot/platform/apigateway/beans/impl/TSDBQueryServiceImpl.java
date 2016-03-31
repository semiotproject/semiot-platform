package ru.semiot.platform.apigateway.beans.impl;

import org.apache.jena.rdf.model.Model;
import ru.semiot.platform.apigateway.beans.TSDBQueryService;
import rx.Observable;

import javax.ejb.Singleton;
import javax.enterprise.inject.Default;

@Singleton
@Default
public class TSDBQueryServiceImpl implements TSDBQueryService {

    @Override
    public Observable<String> queryTimeOfLatestBySensorUri(String systemId, String sensorUri) {
        return null;
    }

    @Override
    public Observable<String> queryTimeOfLatestBySystemId(String systemId) {
        return null;
    }

    @Override
    public Observable<Model> queryLatestBySystemId(String systemId) {
        return null;
    }

    @Override
    public Observable<Model> queryBySensorUri(String systemId, String sensorUri, String start, String end) {
        return null;
    }

    @Override
    public Observable<Model> queryBySystemId(String systemId, String start, String end) {
        return null;
    }
}
