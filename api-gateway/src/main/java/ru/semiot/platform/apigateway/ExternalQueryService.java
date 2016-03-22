package ru.semiot.platform.apigateway;

import java.io.InputStream;
import java.io.StringReader;

import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.aeonbits.owner.ConfigFactory;
import org.glassfish.jersey.client.rx.Rx;
import org.glassfish.jersey.client.rx.rxjava.RxObservableInvoker;

import rx.Observable;
import rx.exceptions.Exceptions;

@Singleton
public class ExternalQueryService {

	private static final ServerConfig config = ConfigFactory
			.create(ServerConfig.class);

	private static final String urlRs = config.consoleEndpoint()
			+ "/jersey-http-service";
	private static final String urlRsRemoveFromFuseki = urlRs
			+ "/remove/fuseki/";
	private static final String urlRsRemoveFromTsdb = config
			.archivRestEndpoint() + "/remove/metric";

	@javax.annotation.Resource
	ManagedExecutorService mes;

	public Observable<JsonArray> getDriversJsonArray() {
		Observable<Response> get = getObservableRespForPath(
				config.repositoryEndpoint());

		return get.map((response) -> {
			try (JsonReader reader = Json.createReader(
					new StringReader(response.readEntity(String.class)))) {
				JsonObject jsonObject = reader.readObject();
				return jsonObject.getJsonObject("drivers")
						.getJsonArray("driver");
			} catch (Exception e) {
				throw Exceptions.propagate(e);
			}
		});
	}

	public Observable<InputStream> getBundleInputStream(String url) {
		Observable<Response> get = getObservableRespForPath(url);

		return get.map((response) -> {
			return response.readEntity(InputStream.class);
		});
	}

	public Observable<Response> sendRsRemoveFromTsdb(JsonObject metrics) {
		return Rx.newClient(RxObservableInvoker.class, mes)
				.target(urlRsRemoveFromTsdb).request().rx()
				.post(Entity.entity(metrics.toString(), MediaType.TEXT_PLAIN));
	}

	public Observable<Response> sendRsRemoveFromFuseki(String pid) {
		return getObservableRespForPath(urlRsRemoveFromFuseki + pid);
	}

	private Observable<Response> getObservableRespForPath(String url) {
		return Rx.newClient(RxObservableInvoker.class, mes).target(url)
				.request().rx().get();
	}

}
