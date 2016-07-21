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

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>SemIoT Platform | System Settings</title>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700" type="text/css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.5.9/css/bootstrap-material-design.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/settings.css">
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
            <div class="col-md-3"></div>
            <div class="col-md-6">
                <div class="well bs-component">
                    <h3>System Settings</h3>
                    <form action="${pageContext.request.contextPath}/config/ConfigurationDriver" method="post">
                        <div class="form-group is-empty">
                            <label for="inputEmail" class="col-md-2 control-label"><%=jsonDomain.get("name")%></label>
                            <div class="col-md-10">
                                <input
                                    class="form-control"
                                    type="text"
                                    id="txtfld1"
                                    name=<%=BundleConstants.MANAGER_DOMAIN%>
                                    value=<%=jsonDomain.get("value")%>
                                />
                            </div>
                            <span class="material-input"></span>
                        </div>
                        <div style="text-align: center;">
                            <button class="btn btn-lg btn-primary btn-raised save-platform-settings-button" type="submit">Save <i class="fa fa-save"></i></button>
                        </div>
                        <input type="hidden" name="configure" value="Save" />
                        <input type="hidden" name="pid" id="pid" value=<%=BundleConstants.MANAGER_PID%> />
                    </form>
                </div>
            </div>
        </div>
    </div>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script src="https://fezvrasta.github.io/bootstrap-material-design/dist/js/material.min.js"></script>
    <script>$.material.init();</script>
    <script src="/js/config/platform-check.js"></script>
</body>
</html>

