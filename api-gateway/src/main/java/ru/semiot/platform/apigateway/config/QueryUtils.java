package ru.semiot.platform.apigateway.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QueryUtils {
		public static HttpClientConfig clientConfig = new HttpClientConfig();
	
	
	public static JSONArray getBundlesJsonArray() throws Exception {
		JSONObject jsonObject = new JSONObject(clientConfig.sendGetUrl(BundleConstants.urlBundlesJson, null, true));
		return jsonObject.getJSONArray("data");
	}
	
	public static List<String> getPidListBundles() throws Exception {
		List<String> pid = new ArrayList<String>();
		JSONObject jsonObject = new JSONObject(clientConfig.sendGetUrl(BundleConstants.urlBundlesJson, null, true));
		JSONArray jArr = jsonObject.getJSONArray("data");
		
		for( int i = 0; i < jArr.length(); i++ ) {
			JSONObject jObj = jArr.getJSONObject(i);
			int id = jObj.getInt("id");
			if(id >= BundleConstants.countDefaultBundles) {
				pid.add(jObj.getString("symbolicName"));
			}
		}
		
		return pid;
	}
	
	public static JSONArray getDriversJsonArray() throws Exception {
		JSONObject jsonObject = new JSONObject(clientConfig.sendGetUrl(BundleConstants.urlDriversJson, null, false));
		return jsonObject.getJSONObject("drivers").getJSONArray("driver");
	}
	
	public static JSONObject getConfiguration(String pid) throws JSONException, Exception {
		JSONObject jsonObject = new JSONObject(clientConfig.sendPost(BundleConstants.urlConfigMgr + pid, null, null));
		return jsonObject.getJSONObject("properties");
	}
	
	public static boolean managerIsConfigurated() throws Exception {
		// TODO костыльное решение
		String services = clientConfig.sendGetUrl(BundleConstants.urlServicesJson, null, true);
		return services.indexOf(BundleConstants.managerApi) == -1 ? false : true;
	}
	
	public static void uninstall(String id_bundle) throws Exception {
		HashMap<String, Object> payload = new HashMap<String, Object>();
		payload.put("action", "uninstall");
		
		String url = BundleConstants.urlBundles + "/" + id_bundle;
		clientConfig.sendPost(url, null, payload);
	}
	
}
