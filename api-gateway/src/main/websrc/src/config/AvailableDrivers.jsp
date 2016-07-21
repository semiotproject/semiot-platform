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

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>SemIoT Platform | Available Drivers</title>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700" type="text/css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.5.9/css/bootstrap-material-design.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/drivers.css">
</head>
<body>
    <div class="navbar navbar-default">
        <div class="container-fluid container">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="/">SemIoT Platform</a>
            </div>
            <ul class="nav navbar-nav">
                <li>
                    <a href="/systems">Explorer</a>
                </li>
                <li class="dropdown active">
                    <a data-target="#" class="dropdown-toggle" data-toggle="dropdown">Configuration
                    <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li class="dropdown-header">Drivers</li>
                        <li><a href="/config/DriversInstalled">Installed</a></li>
                        <li><a href="/config/AvailableDrivers">Available</a></li>
                        <li><a href="/config/UploadDriver">New</a></li>
                        <li class="divider"></li>
                        <li class="dropdown-header">Settings</li>
                        <li><a href="/config/SystemSettings">System</a></li>
                        <li><a href="/config/AdminPanel">Users</a></li>
                    </ul>
                </li>
            </ul>
            <ul class="nav navbar-nav navbar-right">
                <li class="dropdown">
                    <a data-target="#" class="dropdown-toggle" data-toggle="dropdown"><%=request.getRemoteUser()%>
                    <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li><a href="/user/logout">Logout</a></li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
    <div class="container">
        <div class="main-wrapper">
            <h3>Available drivers</h3>
            <form
                action="${pageContext.request.contextPath}/config/AvailableDrivers"
                method="post"
                id="form">
                <div class="table-responsive system-list">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>
                                    <label>№</label>
                                </th>
                                <th>
                                    <label>Name</label>
                                </th>
                                <th>
                                    <label>Action</label>
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                boolean isMissing = true;
                                int j = 1;
                                for (int i = 0; i < jsonBundles.size(); i++) {
                                    JsonObject bundle = jsonBundles.getJsonObject(i);
                                    if (!listInstalledBundles.contains(bundle.getString("pid"))) {
                                        isMissing = false;
                                        String url = bundle.getString("url");
                            %>
                                <tr>
                                    <td>
                                        <span><%=j++%></span>
                                    </td>
                                    <td>
                                        <span><%=bundle.get("name")%></span>
                                    </td>
                                    <td>
                                        <button class="btn btn-primary btn-sm install-button"
                                            data-url='<%=url%>'
                                        >Install</button>
                                </tr>
                            <%
                                }
                            }
                                if (isMissing) {
                            %>
                            <tr>
                                <td></td>
                                <td>Available drivers are missing.</td>
                                <td></td>
                            </tr>
                            <%
                                }
                            %>
                        </tbody>
                    </table>

                    <input name="install" value="install" type="hidden"/>
                    <input type="hidden" name="url" id="url" />
                </div>
            </form>
        </div>
    </div>


    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.5.9/js/material.min.js"></script>
    <script>$.material.init();</script>
    <script src="/js/config/platform-check.js"></script>
    <script>
        $('.install-button').on('click', function() {
            var url = $(this).attr('data-url');
            $('#url').val(url);
            console.info('submitting form with url = ', url);
            $('#form').submit();
        })
    </script>
</body>
</html>

