package ru.semiot.platform.apigateway.config;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarInputStream;

import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.mail.iap.Response;



public class HttpClientConfig {
	
	// TODO Authorization костыль
	public String sendGetUrl(String url, HashMap<String, String> urlpParam, boolean autorizationWebConsole) throws Exception {
		StringBuffer allUrl = new StringBuffer(url);
		
		if(urlpParam != null) {
			allUrl.append("?");
			boolean isFirst = true;
			for(Entry<String, String> entry : urlpParam.entrySet()) {
				if(isFirst) {
					isFirst = false;
				} else {
					allUrl.append("&");
				}
				allUrl.append(entry.getKey()).append("=").append(entry.getValue());
			}
		}
		
		InputStream is = sendGet(allUrl.toString(), autorizationWebConsole);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(is));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		return response.toString();

	}
	
	private InputStream sendGet(String url, boolean autorizationWebConsole) throws Exception {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		if(autorizationWebConsole) {
			con.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");
		}
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		return con.getInputStream();
	}
	
	
	
	// HTTP POST request
	// example payload="{\"jsonrpc\":\"2.0\",\"method\":\"changeDetail\",\"params\":[{\"id\":11376}],\"id\":2}";
	public String sendPost(String url, HashMap<String, String> hashMap, HashMap<String, Object> payload) throws Exception {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", "Basic YWRtaW46YWRtaW4=");
		con.setRequestProperty("Accept", "application/json");
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		// con.setRequestProperty("post", "true"); // TODO
		
		if(hashMap != null) {
			for(Entry<String, String> entry : hashMap.entrySet()) {
				con.setRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		
		con.setUseCaches (false);
		con.setDoInput(true);
		con.setDoOutput(true);
		
		if(payload != null) {
			StringBuilder postData = new StringBuilder();
	        for (Map.Entry<String,Object> param : payload.entrySet()) {
	            if (postData.length() != 0) postData.append('&');
	            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
	            postData.append('=');
	            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	        }
	        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
		
			con.getOutputStream().write(postDataBytes);
		}

		// 
		int responseCode = con.getResponseCode();
		System.out.println("Response Code : " + responseCode);
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		//
		con.disconnect();
		
		return response.toString();
	}
	
	public String sendUploadAvalaibleFile(String urlFrom, String urlTo) {
		try {
			return sendPostUploadFile(urlTo, sendGet(urlFrom, false), urlFrom);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}
	
	public String sendPostUploadFile(String url, InputStream inputStream, String filename) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		httppost.addHeader("Authorization", "Basic YWRtaW46YWRtaW4=");
		httppost.addHeader("Accept", "application/json"); 
		//httppost.addHeader("Content-type", "multipart/form-data");
		HttpResponse response = null;
		String symbolicName = null;
		try {
			byte[] byteArray = IOUtils.toByteArray(inputStream);     
		    InputStream input1 = new ByteArrayInputStream(byteArray);
		    InputStream input2 = new ByteArrayInputStream(byteArray);
		    
			JarInputStream jis = new JarInputStream(input1);
			symbolicName = jis.getManifest().getMainAttributes().getValue("Bundle-SymbolicName");
			
			InputStreamBody bin = new InputStreamBody(input2, filename);
			StringBody action = new StringBody("install");
			
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("bundlefile", bin);
			reqEntity.addPart("action", action);
			
			httppost.setEntity(reqEntity);

			response = httpclient.execute(httppost);

			
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		if (response != null) {
			System.out.println(response.getStatusLine().toString());
		}
		
		return symbolicName;
	}
	
}
