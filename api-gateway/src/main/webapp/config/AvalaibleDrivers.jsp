<%@page import="java.util.HashMap"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONObject"%>
<%@page import="ru.semiot.platform.apigateway.config.HttpClientConfig"%>
<%@page import="java.util.Iterator"%>

<%
	String urlDrivers = "https://raw.githubusercontent.com/semiotproject/semiot-platform/bundles/drivers.json";
	HashMap<String, String> hmap = new HashMap<String, String>(); 
	// hmap.put("post", "true");
	HttpClientConfig clientConfig = new HttpClientConfig();
	JSONObject jsonObject = new JSONObject(clientConfig.sendGetUrl(urlDrivers, null, false));
	JSONArray jsonBundles = jsonObject.getJSONObject("drivers").getJSONArray("driver");
%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
  <title>Avalaible Drivers</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
  <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
</head>
	<body>
		<div class="container">
		<h3>Avalaible Drivers</h3>
			<ul class="nav nav-pills nav-justified">
				<li><a href="/config/SystemSettings">System Settings</a></li>
				<li class="active"><a href="/config/UploadDriver">Drivers</a></li>
			</ul>
			<div class="text-center">
				<br/>
				<div class="btn-group">
				  <a href="/config/DriversInstalled" class="btn btn-primary" role="button">Installed</a>
				  <a href="/config/AvalaibleDrivers" class="btn btn-primary" role="button">Avalaible</a>
				  <a href="/config/UploadDriver" class="btn btn-primary" role="button">Upload</a>
				</div>
			</div>
			<form action="${pageContext.request.contextPath}/AvalaibleDriversHandler" method="post">
				<table class="table table-hover">
				<CAPTION>Список доступных драйверов</CAPTION>
		            <tr>
		            <tr>
		            	<th>№</th>
		                <th>Name</th>
		                <th>Command</th>
		            </tr>
					<%
					for( int i = 0; i < jsonBundles.length(); i++ ) {
						JSONObject jObj = jsonBundles.getJSONObject(i);
						String url = jObj.getString("url");
		           		%>
			           	<tr>	
			           		<td><%=i+1%>
			                <td><%=jObj.get("name")%>
			                <td><input type="submit" class="btn btn-primary btn-sm" name="install" value="install" class="form-control"
			                		onClick="ChangeValue('url', '<%=url%>');" />
			            </tr>
			           	<%
					}
		            %> 
		        </table>
		        <input type="hidden" name="url" id="url" />
			</form>
		
		<script>
		function ChangeValue(id, url)
		{
		    document.getElementById(id).value=url;
		}
		</script>
	</body>
</html>