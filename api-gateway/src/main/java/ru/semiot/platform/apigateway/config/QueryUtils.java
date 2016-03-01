package ru.semiot.platform.apigateway.config;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.utils.JsonUtils;

import ru.semiot.commons.namespaces.Hydra;
import ru.semiot.platform.apigateway.SPARQLQueryService;
import ru.semiot.platform.apigateway.rest.ResourceUtils;
import ru.semiot.platform.apigateway.utils.RDFUtils;
import rx.exceptions.Exceptions;

public class QueryUtils {
	@Inject
    static SPARQLQueryService query;
	
    public static HttpClientConfig clientConfig = new HttpClientConfig();

    public static JsonArray getBundlesJsonArray() throws Exception {
        String json = clientConfig.sendGetUrl(
                BundleConstants.urlBundlesJson, null, true);
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonObject jsonObject = reader.readObject();
            return jsonObject.getJsonArray("data");
        }
    }

    public static List<String> getPidListBundles() throws Exception {
        List<String> pid = new ArrayList<>();
        String json = clientConfig.sendGetUrl(
                BundleConstants.urlBundlesJson, null, true);
        try(JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonObject jsonObject = reader.readObject();
            JsonArray jArr = jsonObject.getJsonArray("data");

            for (int i = 0; i < jArr.size(); i++) {
                JsonObject jObj = jArr.getJsonObject(i);
                int id = jObj.getInt("id");
                if (id >= BundleConstants.countDefaultBundles) {
                    pid.add(jObj.getString("symbolicName"));
                }
            }

            return pid;
        }
    }

    public static JsonArray getDriversJsonArray() throws Exception {
        String json = clientConfig.sendGetUrl(
                BundleConstants.urlDriversJson, null, false);
        try(JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonObject jsonObject = reader.readObject();
            return jsonObject.getJsonObject("drivers").getJsonArray("driver");
        }
    }

    public static JsonObject getConfiguration(String pid) throws Exception {
        String json = clientConfig.sendPost(
                BundleConstants.urlConfigMgr + pid, null);
        // когда использую пост, через раз вместо имени приходит pid
        try(JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonObject jsonObject = reader.readObject();
            /*
             * String res = clientConfig.sendGetUrl(BundleConstants.urlConfigMgr +
             * pid + ".json", null, true); JSONObject jsonObject; if(
             * res.indexOf("[") == 0 && res.lastIndexOf("]") == res.length() - 1 ) {
             * jsonObject = new JSONObject(res.substring(1, res.length() - 1)); }
             * else { jsonObject = new JSONObject(res); }
             */
            return jsonObject.getJsonObject("properties");
        }
    }

    public static boolean managerIsConfigurated() throws Exception {
        // TODO правильное решение через сервисы? его можно было бы заменить на
        // конфигурации
        String services = clientConfig.sendGetUrl(
                BundleConstants.urlServicesJson, null, true);
        System.out.println(BundleConstants.urlServicesJson);
        System.out.println(services);
        return services.contains(BundleConstants.managerApi);
    }

    public static String getStatusConfigurations() throws Exception {
        return clientConfig.sendGetUrl(
                BundleConstants.urlStatusConfigurationsJson, null, true);
    }

    public static void uninstall(String id_bundle) throws Exception {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("action", "uninstall");

        String url = BundleConstants.urlBundles + "/" + id_bundle;
        clientConfig.sendPost(url, null, payload);

        deleteConfig(id_bundle);
    }
    
    public static void uninstallWithData(String id_bundle) throws Exception {
    	uninstall(id_bundle);
    	Observable <List<String>> systems_id = query.select(BundleConstants.queryIdSystemsForDriver.replace("${DRIVER}", 
    			"id_bundle"))
    			.map((ResultSet rs) -> {
    				List<String> list = new ArrayList<String>();
                    while (rs.hasNext()) {
                        QuerySolution qs = rs.next();
                        Literal id = qs.getLiteral("id");
                        list.add(id.getString());
                    }

                    return list;
                });
    	//systems_id.
                
        clientConfig.sendGetUrl(
                BundleConstants.urlRsRemoveFromTsdb + id_bundle, null, false);
                
    	clientConfig.sendGetUrl(
                BundleConstants.urlRsRemoveFromFuseki + id_bundle, null, false);
    }

    private static void deleteConfig(String id_bundle) throws Exception {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("delete", "true");
        payload.put("apply", "true");

        clientConfig.sendPost(BundleConstants.urlConfigMgr + id_bundle, null,
                payload);
    }

    public static void start(String id_bundle) throws Exception {
        HashMap<String, Object> payload = new HashMap<>();
        payload.put("action", "start");

        String url = BundleConstants.urlBundles + "/" + id_bundle;
        clientConfig.sendPost(url, null, payload);
    }

    public static void sendGetUrl(String url,
            HashMap<String, String> parameters, boolean autoriz)
            throws Exception {
        clientConfig.sendGetUrl(url, parameters, autoriz);
    }
}
