<%@page import="java.util.Set"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="javax.json.JsonReader"%>
<%@page import="java.util.Iterator"%>

<%
    String keyType = "@type";
    String commonSchemaType = "semiot:CommonSchema";
    String repeatableSchemaType = "semiot:RepeatableSchema";

    JsonObject jsonConfig = (JsonObject) session.getAttribute("jsonConfig");
    String pid = jsonConfig.getString("semiot:driverPid");
    JsonArray jsonArray = jsonConfig.getJsonArray("semiot:view");
    Integer repeatbleMax = jsonConfig.getInt("semiot:maxRepeatableSchemas");

    session.setAttribute("pid", pid);

    int j = 0;

    JsonObject repeatbleJson = null;
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
ыскшзе
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
        <h3>Configuration Driver</h3>
        <form
        </form>
</body>
</html>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>SemIoT Platform | Driver Configuration</title>
    <link rel="stylesheet" href="http://fonts.googleapis.com/css?family=Roboto:300,400,500,700" type="text/css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.5.9/css/bootstrap-material-design.min.css">
    <link rel="stylesheet" href="http://cdn.leafletjs.com/leaflet-0.7/leaflet.css" />
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet.draw/0.2.3/leaflet.draw.css">
    <link rel="stylesheet" href="/static/css/common.css">
    <link rel="stylesheet" href="/static/css/drivers.css">
    <script>var CONFIG = JSON.parse('<%=jsonArray%>');</script>
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
                    <a href="/explorer/">Explorer</a>
                </li>
                <li class="dropdown active">
                    <a href="/drivers/" data-target="#" class="dropdown-toggle" data-toggle="dropdown">Drivers
                    <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li><a href="/drivers/installed/">Installed</a></li>
                        <li><a href="/drivers/available/">Available</a></li>
                        <li><a href="/drivers/new/">New</a></li>
                    </ul>
                </li>
                <li class="dropdown">
                    <a href="bootstrap-elements.html" data-target="#" class="dropdown-toggle" data-toggle="dropdown">Settings
                    <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li><a href="/settings/system/">System</a></li>
                        <li><a href="/settings/users/">Users</a></li>
                    </ul>
                </li>
            </ul>
            <ul class="nav navbar-nav navbar-right">
                <li class="dropdown">
                    <a href="bootstrap-elements.html" data-target="#" class="dropdown-toggle" data-toggle="dropdown">root
                    <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li><a href="/logout/">Logout</a></li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
    <div class="container">
        <ul class="breadcrumb">
            <li class="active"><a href="/">Home</a></li>
            <li class="active"><a href="/">Drivers</a></li>
            <li>New driver</li>
        </ul>
        <h3>New driver configuration</h3>
        <form
            method="POST"
            action="${pageContext.request.contextPath}/config/ConfigurationDriver"
        >

            <div class="container">
                <div class="well bs-component regular-fields">
                </div>
            </div>

            <h3>Repeatable configuration</h3>
            <div class="container">
                <div class="well bs-component">
                    <div id="map" style="height: 500px;"></div>
                </div>
            </div>
            <div style="text-align: center;">
                <button class="btn btn-raised btn-info btn-lg">Save <i class="fa fa-save"></i></button>
            </div>
        </form>
    </div>


    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script src="http://fezvrasta.github.io/bootstrap-material-design/dist/js/material.min.js"></script>
    <script src="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet.draw/0.2.3/leaflet.draw.js"></script>
    <script>$.material.init();</script>
    <script>
        console.log(CONFIG);
    /*
        var map = L.map('map').setView([51.505, -0.09], 13);

        L.tileLayer('http://korona.geog.uni-heidelberg.de/tiles/roadsg/x={x}&y={y}&z={z}', {
            attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);

        var drawnItems = new L.FeatureGroup();
        map.addLayer(drawnItems);

        var drawControl = new L.Control.Draw({
            position: 'topright',
            draw: {
                rect: {
                    shapeOptions: {
                        color: 'orange'
                    },
                },
                polyline: false,
                marker: false,
                polygon: false,
                circle: false
            },
            edit: {
                featureGroup: drawnItems
            }
        });
        map.addControl(drawControl);

        map.on('draw:created', function (e) {
            drawnItems.addLayer(e.layer);
        });
    */
    </script>
</body>
</html>