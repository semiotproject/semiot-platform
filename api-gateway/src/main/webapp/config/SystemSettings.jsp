<%@page import="java.util.HashMap"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="java.util.Iterator"%>
<%@page import="ru.semiot.platform.apigateway.config.BundleConstants"%>

<%
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setHeader("Expires", "0");

    JsonObject jsonDomain = (JsonObject) request.getAttribute("jsonDomain");
    boolean managerIsConfigurated = (Boolean) request.getAttribute(
    		"mngrIsConfigurated");
%>


<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>

<head>
<title>System Settings</title>
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
		<h3>System Settings</h3>
		<ul class="nav nav-pills nav-justified">
			<li class="active"><a href="/config/SystemSettings">System
					Settings</a></li>
			<li><a href="/config/DriversInstalled">Drivers</a></li>
		</ul>
		<form
			action="${pageContext.request.contextPath}/config/ConfigurationDriver"
			method="post">
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
						<td><input type="text" id="txtfld1"
							onClick="SelectAll('txtfld1');"
							name=<%=BundleConstants.managerDomain%> style="width: 200px"
							value=<%=jsonDomain.get("value")%> />
					</tr>
				</table>
			</div>
			<div class="text-right">
				<input class="btn btn-primary btn-sm" type="submit" name="configure"
					value="Save" <% if (managerIsConfigurated) { %> disabled <% }%> />
			</div>
			<input type="hidden" name="pid" id="pid"
				value=<%=BundleConstants.managerPid%> />
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

