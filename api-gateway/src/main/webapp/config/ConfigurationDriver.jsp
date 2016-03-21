<%@page import="java.util.Set"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="javax.json.JsonReader"%>
<%@page import="java.util.Iterator"%>

<%
	JsonArray jsonArray = (JsonArray) session.getAttribute("jsonConfig");
	int j = 0;
%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<title>Configuration Driver</title>
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
		<h3>Configuration Driver</h3>
		<form
			action="${pageContext.request.contextPath}/config/ConfigurationDriver"
			method="post">
			<div class="small-table">
				<table class="table table-hover">
					<tr>
					<tr>
						<th>№</th>
						<th>Name</th>
						<th>Value</th>
					</tr>
					<%
						if (jsonArray != null) {
							for (int i = 0; i < jsonArray.size(); i++) {
								JsonObject jo = jsonArray.getJsonObject(i);
								Set<String> it = jo.keySet();
								// int j = 0;
								for (String key : it) {
									if (!key.equals("@type")) {
										JsonObject jsonProperty = jo.getJsonObject(key);
										if (key.equals("ru.semiot.area")) {
											Set<String> it1 = jsonProperty.keySet();
											// int j = 0;
											for (String key1 : it1) {
												if (!key1.equals("@type") && !key1.equals("rdfs:label")) {
													JsonObject jsonProperty1 = jsonProperty.getJsonObject(key1);
													String name = null;
													if (key1.equals("ru.semiot.area.latitude_ne")) {
														name = "ru.semiot.area.1.1.latitude";
													}
													if (key1.equals("ru.semiot.area.longitude_ne")) {
														name = "ru.semiot.area.1.1.longitude";
													}
													if (key1.equals("ru.semiot.area.latitude_sw")) {
														name = "ru.semiot.area.1.2.latitude";
													}
													if (key1.equals("ru.semiot.area.longitude_sw")) {
														name = "ru.semiot.area.1.2.longitude";
													}
					%>
					<td><%=++j%>
					<td><%=name%>
					<td><input type="text" id="txtfld<%=j%>"
						onClick="SelectAll('txtfld<%=j%>');" name=<%=name%>
						style="width: 200px"
						value=<%=jsonProperty1.get("dtype:defaultValue")%> />
					</tr>
					<%
						}
											}
										} else {
					%>
					<td><%=++j%>
					<td><%=jsonProperty.get("rdfs:label")%>
					<td><input type="text" id="txtfld<%=j%>"
						onClick="SelectAll('txtfld<%=j%>');" name=<%=key%>
						style="width: 200px"
						value=<%=jsonProperty.get("dtype:defaultValue")%> />
					</tr>
					<%
						}
									}
								}
							}
						}
						if (j < 1) {
					%>
					<tr>
						<td>Configuration is not found.</td>
					</tr>
					<%
						}
					%>
				</table>
			</div>
			<div class="text-right">
				<%
					if (j > 0) {
				%>
				<input class="btn btn-primary btn-sm" type="submit" name="save"
					value="Save and start" />
				<%
					}
				%>
				<input class="btn btn-primary btn-sm" type="submit" name="back"
					value="Back" />
			</div>
		</form>

		<script>
			function SelectAll(id) {
				document.getElementById(id).focus();
				document.getElementById(id).select();
			}
		</script>
</body>
</html>