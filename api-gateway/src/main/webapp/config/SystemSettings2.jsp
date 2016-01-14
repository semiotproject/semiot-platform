
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
	JSONObject jsonObject = clientConfig.sendPost(url, hmap);
	JSONObject jsonProperties = jsonObject.getJSONObject("properties");
%>


<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>System Settings</title>
	<link rel="stylesheet" type="text/css" href="/WEB-INF/css/classes.css"/>
</head>
	<body>
		<div id="roundbar-blue">
			<ul>
				<li class="first"><a href="/">System Settings</a></li>
				<li class="active"><a href="#">Drivers</a></li>
			</ul>
		</div>
		<div class="container">
			<form action="${pageContext.request.contextPath}/SystemSettingsHandler" method="post">
			    <div class="small-table">
			        <h2>System Settings</h2>
			        <table>
			            <tr>
			            <tr>
			            	<th>№</th>
			                <th>Name</th>
			                <th>Value</th>
			                <th>Optional</th>
			            </tr>
			            <tr>
			                <%
		                	Iterator<String> it = jsonProperties.keys();
		                	int i = 0;
		                	while(it.hasNext()) {
		                		String key = it.next();
		                		JSONObject jsonProperty = jsonProperties.getJSONObject(key);
			                %>
			                <td><%=++i%>
			                <td><%=jsonProperty.get("name")%>
			                <td><input type="text" id="txtfld<%=i%>" onClick="SelectAll('txtfld<%=i%>');" 
			                	name=<%=key%> style="width:200px" 
			                	value = <%=jsonProperty.get("value")%> />
			                <td><%=jsonProperty.get("optional")%>
			            </tr>
			            <%
				            }
				        %>
			        </table>
			    </div> 
		    
			    <div class="ajax-user-form">
			        <div>
			            <input type="submit" name="save" value="Save" class="form-control"/>
			        </div>
			    </div>
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

