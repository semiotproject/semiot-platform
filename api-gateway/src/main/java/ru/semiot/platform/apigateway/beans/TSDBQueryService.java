package ru.semiot.platform.apigateway.beans;

import java.time.ZonedDateTime;
import java.util.List;

import javax.json.JsonArray;
import javax.ws.rs.core.Response;

import org.apache.jena.rdf.model.Model;

import rx.Observable;

public interface TSDBQueryService {

    public Observable<String> queryTimeOfLatestBySystemId(String systemId,
            List<String> sensorsId);

    public Observable<Model> queryLatestBySystemId(String systemId,
            List<String> sensorsId);

    public Observable<Model> queryBySystemId(String systemId,
            List<String> sensorsId, String start, String end);

    public Observable<Response> remove(JsonArray array);

    public Observable<ZonedDateTime> queryDateTimeOfLatestActuation(
            String systemId);

    public Observable<Model> queryActuationsByRange(String systemId,
            ZonedDateTime start, ZonedDateTime end);
}
