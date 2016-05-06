package ru.semiot.platform.apigateway.beans;

import org.apache.jena.rdf.model.Model;
import rx.Observable;

import java.time.ZonedDateTime;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;

public interface TSDBQueryService {

  public Observable<String> queryTimeOfLatestBySystemId(String systemId,
                                                        List<String> sensorsId);

  public Observable<Model> queryLatestBySystemId(String systemId,
                                                 List<String> sensorsId);

  public Observable<Model> queryBySystemId(String systemId,
                                           List<String> sensorsId, String start, String end);

  public Observable<Response> remove(JsonArray array);

  public Observable<ZonedDateTime> queryDateTimeOfLatestCommandResult(
      String systemId);

  public Observable<Model> queryCommandResultsByRange(String systemId,
                                                  ZonedDateTime start, ZonedDateTime end);

  public Observable<Response> sendSettingsAsPost(JsonObject json);
}
