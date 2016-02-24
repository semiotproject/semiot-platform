<%@page import="java.util.Set"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="ru.semiot.platform.apigateway.config.QueryUtils"%>
<%@page import="java.util.Iterator"%>

<%
    String pid = null;
    if (request.getParameter("symbolicName") != null) {
        pid = String.valueOf(request.getParameter("symbolicName"));
    }

    JsonObject jsonProperties = QueryUtils.getConfiguration(pid);

    for (int i = 0; StringUtils.isNotBlank(pid) && jsonProperties.size() < 1 && i < 10; i++) {
        jsonProperties = QueryUtils.getConfiguration(pid);
        Thread.sleep(300);
    }
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
                action="${pageContext.request.contextPath}/ConfigurationDriverHandler"
                method="post">
                <div class="small-table">
                    <table class="table table-hover">
                        <tr>
                        <tr>
                            <th>№</th>
                            <th>Name</th>
                            <th>Value</th>
                            <th>Optional</th>
                        </tr>
                        <%
                            if (jsonProperties.size() < 1) {
                        %>
                        <tr>
                            <td>Configuration is not found.</td>
                        </tr>
                        <%
                        } else {
                            Set<String> it = jsonProperties.keySet();
                            int i = 0;
                            for (String key : it) {
                                JsonObject jsonProperty = jsonProperties.getJsonObject(key);
                        %>
                        <td><%=++i%>
                        <td><%=jsonProperty.get("name")%>
                        <td><input type="text" id="txtfld<%=i%>"
                                   onClick="SelectAll('txtfld<%=i%>');" name=<%=key%>
                                   style="width: 200px" value=<%=jsonProperty.get("value")%> />
                        <td><%=jsonProperty.get("optional")%>
                            </tr>
                            <%
                                    }
                                }
                            %>
                    </table>
                </div>
                <div class="text-right">
                    <%
                        if (jsonProperties.size() > 0) {
                    %>
                    <input class="btn btn-primary btn-sm" type="submit" name="save"
                           value="Save and start" /> <a href="#myModal"
						class="btn btn-primary btn-sm" data-toggle="modal">uninstall</a>
                    <%
                        }
                    %>
                    <input class="btn btn-primary btn-sm" type="submit" name="back"
                           value="Back" />
                </div>
                
                <input type="hidden" name="pid" id="pid" value=<%=pid%> />
                
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

            <script>
                function SelectAll(id) {
                    document.getElementById(id).focus();
                    document.getElementById(id).select();
                }
            </script>
    </body>
</html>