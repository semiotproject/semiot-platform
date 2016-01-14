<%@page import="java.util.HashMap"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONObject"%>
<%@page import="ru.semiot.platform.apigateway.config.HttpClientConfig"%>
<%@page import="java.util.Iterator"%>

<%
	int countDefaultBundles = 14;
	String urlBundles = "http://localhost:8181/system/console/bundles.json";
	HashMap<String, String> hmap = new HashMap<String, String>(); 
	hmap.put("post", "true");
	HttpClientConfig clientConfig = new HttpClientConfig();
	JSONObject jsonObject = new JSONObject(clientConfig.sendGetUrl(urlBundles, hmap, true));
	JSONArray jsonBundles = jsonObject.getJSONArray("data");
%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
  <title>Drivers Installed</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
  <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
</head>
	<body>
		<div class="container">
		<h3>Drivers Installed</h3>
			<ul class="nav nav-pills nav-justified">
				<li><a href="/config/SystemSettings">System Settings</a></li>
				<li class="active"><a href="/config/DriversInstalled">Drivers</a></li>
			</ul>
			<div class="text-center">
				<br/>
				<div class="btn-group">
				  <a href="/config/DriversInstalled" class="btn btn-primary" role="button">Installed</a>
				  <a href="/config/AvalaibleDrivers" class="btn btn-primary" role="button">Avalaible</a>
				  <a href="/config/UploadDriver" class="btn btn-primary" role="button">Upload</a>
				</div>
			</div>
			<form action="${pageContext.request.contextPath}/DriverInstalledHandler" method="post">
				<table class="table table-hover">
				<CAPTION>Список драйверов</CAPTION>
		            <tr>
		            <tr>
		            	<th>№</th>
		                <th>Name</th>
		                <th>Select</th>
		            </tr>
		            
		            <%
					if(jsonBundles.length() <= countDefaultBundles) {
					%>
					<tr>
						<td>Драйвера отсутствуют</td>
					</tr>
					<%} else {
						int j = 1;
						for( int i = 0; i < jsonBundles.length(); i++ ) {
							JSONObject jObj = jsonBundles.getJSONObject(i);
							int id = jObj.getInt("id");
							// System.out.println(id);
							if(id >= countDefaultBundles) {
			           		%>
				           	<tr>	
				           		<td><%=j++%>
				                <td><%=jObj.get("name")%>
				                <td><input type="submit" class="btn btn-primary btn-sm" name="conf" value="configuration" class="form-control" disabled/>
									<input type="submit" class="btn btn-primary btn-sm" name="uninstall" value="uninstall" class="form-control"
										onClick="ChangeValue('id_bundle', '<%=id%>');" /></td>
				            </tr>
				           	<%
				           	}
						}
					}
		            %> 
		        </table>
		        <input type="hidden" name="id_bundle" id="id_bundle" />
			</form>
		</div>
		<script>
		function ChangeValue(id, val)
		{
		    document.getElementById(id).value=val;
		}
		</script>
	</body>
</html>