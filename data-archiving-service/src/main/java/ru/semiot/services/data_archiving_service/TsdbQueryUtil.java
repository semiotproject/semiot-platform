package ru.semiot.services.data_archiving_service;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TsdbQueryUtil {
	
	private static final Logger logger = LoggerFactory
			.getLogger(TsdbQueryUtil.class);
	
	private static HttpClient client = new DefaultHttpClient();
	//http://www.programcreek.com/java-api-examples/org.apache.http.client.methods.HttpDelete
	private static String deleteMetricQeury = "http://opentsdb:4242/api/query?"
			+ "start=2000/01/01-00:00:00&m=sum:${METRIC}";
	
	public static String deleteMetricRequest(String uid) {
		HttpDelete delete = new HttpDelete(deleteMetricQeury.replace("${METRIC}", uid));
		delete.addHeader("Accept", "application/json");
		delete.addHeader("Content-Type", "application/x-www-form-urlencoded");
		delete.getParams().setIntParameter("http.socket.timeout", 2000);

		try {
			HttpResponse httpResponse = client.execute(delete);
			if (httpResponse != null) {
				String res = httpResponse.getStatusLine().toString();
				logger.info(res);
				return res;
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
}
