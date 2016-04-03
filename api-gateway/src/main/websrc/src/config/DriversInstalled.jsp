<%@page import="com.github.jsonldjava.utils.JsonUtils"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="java.util.Iterator"%>
<%@page import="ru.semiot.platform.apigateway.config.BundleConstants"%>
<%@page import="javax.json.Json"%>

<%
	response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
	response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
	response.setHeader("Expires", "0");

	JsonArray jsonBundles = (JsonArray) request.getAttribute("jsonArray");
%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<title>Drivers Installed</title>
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
    <%String username = request.getRemoteUser();%>
        <div class="navbar-form navbar-right" role="navigation">
            <button class="btn btn-primary btn-sm" onClick="logout()" name="logout" readonly>
                <%=username%>  
                <i class="glyphicon glyphicon-log-out"></i>
            </button>                
        </div>
        <script>
            function logout(){
                 $.ajax({url: "${pageContext.request.contextPath}/logout",
                    type: 'GET',
                    success: function(){
                        window.location.replace("/");
                    },
                    error: function () {
                        window.location.reload();
                    }
                });
            }
        </script>
	<div class="container">
		<h3>Drivers Installed</h3>
		<ul class="nav nav-pills nav-justified">
                    <li><a href="/config/AdminPanel">Administration Panel</a></li>
			<li><a href="/config/SystemSettings">System Settings</a></li>
			<li class="active"><a href="/config/DriversInstalled">Drivers</a></li>
		</ul>
		<div class="text-center">
			<br />
			<div class="btn-group">
				<a href="/config/DriversInstalled" class="btn btn-primary active"
					role="button">Installed</a> <a href="/config/AvailableDrivers"
					class="btn btn-primary" role="button">Available</a> <a
					href="/config/UploadDriver" class="btn btn-primary" role="button">Upload</a>
			</div>
		</div>
		<form
			action="${pageContext.request.contextPath}/config/DriversInstalled"
			method="post">
			<table class="table table-hover">
				<CAPTION>List of drivers</CAPTION>
				<tr>
				<tr>
					<th>№</th>
					<th>Name</th>
					<th>Select</th>
				</tr>

				<%
					if (jsonBundles.size() <= BundleConstants.countDefaultBundles) {
				%>
				<tr>
					<td>Installed drivers are missing.</td>
				</tr>
				<%
					} else {
						int j = 1;
						for (int i = 0; i < jsonBundles.size(); i++) {
							JsonObject jObj = jsonBundles.getJsonObject(i);
							int id = jObj.getInt("id");
							if (id >= BundleConstants.countDefaultBundles) {
								String symbName = jObj.getString("symbolicName");
				%>
				<tr>
					<td><%=j++%>
					<td><%=jObj.get("name")%>
					<td><a href="#myModal" class="btn btn-primary btn-sm"
						data-toggle="modal"
						onClick="ChangeValue('id_bundle', '<%=symbName%>');">uninstall</a>
					</td>

				</tr>
				<%
					}
						}
					}
				%>
			</table>

			<input type="hidden" name="id_bundle" id="id_bundle" />

			<div id="myModal" class="modal fade">
				<div class="modal-dialog modal-sm">
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal"
								aria-hidden="true">×</button>
							<h4 class="modal-title">Optional feature</h4>
						</div>
						<!-- Основное содержимое модального окна -->
						<div class="modal-body">Do you want to remove all data
							connected with the driver?</div>
						<!-- Футер модального окна -->
						<div class="modal-footer">
							<input type="submit" class="btn btn-primary btn-sm"
								name="uninstall" value="no" class="form-control" /> <input
								type="submit" class="btn btn-primary btn-sm"
								name="uninstallWithDeleteData" value="yes" class="form-control" />
						</div>
					</div>
				</div>
			</div>
		</form>
	</div>
	<script>
		function ChangeValue(id, val) {
			document.getElementById(id).value = val;
		}
	</script>
</body>
</html>