
<%@page import="java.util.HashMap"%>
<%@page import="org.json.JSONArray"%>
<%@page import="org.json.JSONObject"%>
<%@page import="ru.semiot.platform.apigateway.config.HttpClientConfig"%>
<%@page import="java.util.Iterator"%>

<%
	String url = "http://localhost:8181/system/console/configMgr/ru.semiot.platform.deviceproxyservice.manager";
	HashMap<String, String> hmap = new HashMap<String, String>(); 
	hmap.put("post", "true");
	HttpClientConfig clientConfig = new HttpClientConfig(); 
	// Добавить проверку что сервак не лежит!
	JSONObject jsonObject = new JSONObject(clientConfig.sendPost(url, hmap, null));
	JSONObject jsonProperties = jsonObject.getJSONObject("properties");
	String key = "ru.semiot.platform.deviceproxyservice.manager.domain";
	JSONObject jsonDomain = jsonProperties.getJSONObject(key);
%>


<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
  <title>System Settings</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
  <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
</head>
	<body>
		<div class="container">
		<h3>System Settings</h3>
			<ul class="nav nav-pills nav-justified">
				<li class="active"><a href="/config/SystemSettings">System Settings</a></li>
				<li><a href="/config/DriversInstalled">Drivers</a></li>
			</ul>
			<form action="${pageContext.request.contextPath}/ConfigurationDriverHandler" method="post">
			    <div class="small-table">
			        <h2>System Settings</h2>
			        <table class="table table-hover">
			            <tr>
			            <tr>
			            	<th>№</th>
			                <th>Name</th>
			                <th>Value</th>
			            </tr>
			            <tr>
			                <td><%=1%>
			                <td><%=jsonDomain.get("name")%>
			                <td><input type="text" id="txtfld1" onClick="SelectAll('txtfld1');" 
			                	name=<%=key%> style="width:200px" 
			                	value = <%=jsonDomain.get("value")%> />
			            </tr>
			        </table>
			    </div>
		        <div class="text-right">
		            <button class="btn btn-primary btn-sm" name="save" type="submit">Save</button>
		        </div>
		         <input type="hidden" name="url" id="url" value=<%=url%> />
		    </form>
		</div>  
		
		<script>
		function SelectAll(id)
		{
		    document.getElementById(id).focus();
		    document.getElementById(id).select();
		}
		</script>
	</body>
</html>

