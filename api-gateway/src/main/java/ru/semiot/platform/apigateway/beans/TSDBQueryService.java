package ru.semiot.platform.apigateway.beans;

import org.apache.jena.rdf.model.Model;
import rx.Observable;

import java.time.ZonedDateTime;

public interface TSDBQueryService {

    public Observable<String> queryTimeOfLatestBySensorUri(String systemId, String sensorUri);

    public Observable<String> queryTimeOfLatestBySystemId(String systemId);

    public Observable<Model> queryLatestBySystemId(String systemId);

    public Observable<Model> queryBySensorUri(String systemId, String sensorUri, String start, String end);

    public Observable<Model> queryBySystemId(String systemId, String start, String end);

    public Observable<ZonedDateTime> queryDateTimeOfLatestActuation(String systemId);

    public Observable<Model> queryActuationsByRange(String systemId, ZonedDateTime start, ZonedDateTime end);
}
