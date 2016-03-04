<%@page import="org.apache.jena.atlas.lib.CollectionUtils"%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.List"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="java.util.Iterator"%>

<%
	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
	response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
	response.setHeader("Expires", "0");

	JsonArray jsonBundles = (JsonArray) request.getAttribute("jnBundles");
	List<String> listInstalledBundles = (List<String>) request.getAttribute("lstInstalledBundles");
%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<title>Available Drivers</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet"
	href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<script
	src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<script
	src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
</head>
<body>
	<div class="container">
		<h3>Available Drivers</h3>
		<ul class="nav nav-pills nav-justified">
			<li><a href="/config/SystemSettings">System Settings</a></li>
			<li class="active"><a href="/config/UploadDriver">Drivers</a></li>
		</ul>
		<div class="text-center">
			<br />
			<div class="btn-group">
				<a href="/config/DriversInstalled" class="btn btn-primary"
					role="button">Installed</a> <a href="/config/AvailableDrivers"
					class="btn btn-primary active" role="button">Available</a> <a
					href="/config/UploadDriver" class="btn btn-primary" role="button">Upload</a>
			</div>
		</div>
		<form
			action="${pageContext.request.contextPath}/config/AvailableDrivers"
			method="post">
			<table class="table table-hover">
				<CAPTION>List of available drivers</CAPTION>
				<tr>
				<tr>
					<th>№</th>
					<th>Name</th>
					<th>Command</th>
				</tr>
				<%
					boolean isMissing = true;
					int j = 1;
					for (int i = 0; i < jsonBundles.size(); i++) {
						JsonObject jObj = jsonBundles.getJsonObject(i);
						if (!listInstalledBundles.contains(jObj.getString("pid"))) {
							isMissing = false;
							String url = jObj.getString("url");
				%>
				<tr>
					<td><%=j++%>
					<td><%=jObj.get("name")%>
					<td><input type="submit" class="btn btn-primary btn-sm"
						name="install" value="install" class="form-control"
						onClick="ChangeValue('url', '<%=url%>');" />
				</tr>
				<%
					}
					}
					if (isMissing) {
				%>
				<tr>
					<td>Available drivers are missing.</td>
				</tr>
				<%
					}
				%>
			</table>
			<input type="hidden" name="url" id="url" />
		</form>
		<script>
			function ChangeValue(id, url) {
				document.getElementById(id).value = url;
			}
		</script>
</body>
</html>