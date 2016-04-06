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
    <script>\
        var DRIVER_SCHEMA = JSON.parse('<%=jsonArray%>');
        var MAX_REPEATABLE_COUNT = parseInt('<%=repeatbleMax%>')
    </script>
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
                    <a href="/explorer">Explorer</a>
                </li>
                <li class="dropdown">
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
                    <a data-target="#" class="dropdown-toggle" data-toggle="dropdown">root
                    <b class="caret"></b></a>
                    <ul class="dropdown-menu">
                        <li><a href="/logout">Logout</a></li>
                    </ul>
                </li>
            </ul>
        </div>
    </div>
    <div class="container">
        <h3>
            <span>New driver configuration</span>
            <button class="btn btn-raised btn-primary btn-lg">Save <i class="fa fa-save"></i></button>
        </h3>
        <form
            method="POST"
            action="${pageContext.request.contextPath}/config/ConfigurationDriver"
        >

            <div class="container well bs-component common-fields"></div>

            <h3>
                <span>Repeatable configuration</span>
                <button class="btn btn-raised btn-primary btn-lg add-repeatable">Add <i class="fa fa-plus"></i></button>
            </h3>
            <div class="repeatable-configurations">

            </div>

        </form>
    </div>

    <div class="common-field template" style="display: none;">
        <div class="form-group is-empty">
            <label class="col-md-2 control-label"></label>
            <div class="col-md-10">
                <input
                    class="form-control"
                    type="text"
                />
            </div>
            <span class="material-input"></span>
        </div>
    </div>

    <div class="repeatable-configuration template" style="display: none;">
        <div class="container well bs-component ">
            <button class="btn btn-raised btn-primary btn-lg remove-repeatable">Remove <i class="fa fa-minus"></i></button>
        </div>
    </div>

    <div class="map-wrapper template" style="display: none;">
         <div style="height: 500px;"></div>
    </div>



    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script src="http://fezvrasta.github.io/bootstrap-material-design/dist/js/material.min.js"></script>
    <script src="http://cdn.leafletjs.com/leaflet/v0.7.7/leaflet.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet.draw/0.2.3/leaflet.draw.js"></script>
    <script>$.material.init();</script>
    <script>
        console.log(DRIVER_SCHEMA);

        var commonSchema = (function() {
            return DRIVER_SCHEMA.filter((i) => {
                return i['@type'] === 'semiot:CommonSchema';
            })[0];
        })();

        var repeatableSchema = (function() {
            return DRIVER_SCHEMA.filter((i) => {
                return i['@type'] === "semiot:RepeatableSchema";
            })[0];
        })();

        function getLocationFromSchema(data) {
            return [
                [
                    data['ru.semiot.area.latitude_ne']['dtype:defaultValue'],
                    data['ru.semiot.area.longitude_ne']['dtype:defaultValue'],
                ],
                [
                    data['ru.semiot.area.latitude_sw']['dtype:defaultValue'],
                    data['ru.semiot.area.longitude_ne']['dtype:defaultValue'],
                ],
                [
                    data['ru.semiot.area.latitude_sw']['dtype:defaultValue'],
                    data['ru.semiot.area.longitude_sw']['dtype:defaultValue'],
                ],
                [
                    data['ru.semiot.area.latitude_ne']['dtype:defaultValue'],
                    data['ru.semiot.area.longitude_sw']['dtype:defaultValue'],
                ]
            ];
        }

        function initMap(mapId, data) {
            console.info('initing map with id = ', mapId);
            var map = L.map(mapId).setView([51.505, -0.09], 13);

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

            L.polygon(getLocationFromSchema(data), {
                color: 'orange'
            }).addTo(drawnItems);
        }

        function renderField(f, key) {
            if (!f['rdfs:label']) {
                return null;
            }
            var field = $('.common-field.template').find('.form-group').clone();

            field.find('label').text(f['rdfs:label']);
            field.find('input').val(f['dtype:defaultValue']).attr({
                name: key
            });

            return field;
        }

        function addCommonBlock() {
            Object.keys(commonSchema).forEach(function(key, index) {
                $('.common-fields').append(renderField(commonSchema[key], key));
            });
        }

        function addRepeatableBlock() {
            var formTemplate = $('.repeatable-configuration.template').find('> div').clone();
            formTemplate.appendTo('.repeatable-configurations');
            Object.keys(repeatableSchema).forEach(function(key, index) {

                if (repeatableSchema[key]['@type'] === "semiot:GeoRectangle") {
                    var map = $('.map-wrapper.template').find('> div').clone();

                    // generate random id
                    var mapId = 'map' + Date.now();
                    map.attr({
                        id: mapId
                    });

                    // initialize map

                    map.appendTo(formTemplate);
                    initMap(mapId, repeatableSchema[key]);
                } else {
                    formTemplate.append(renderField(repeatableSchema[key], key));
                }
            });
        }

        function checkMaxRepeatableCount() {
            if ($('.repeatable-configurations').length === MAX_REPEATABLE_COUNT) {
                $('.add-repeatable').attr('disabled', 'disabled');
            } else {
                $('.add-repeatable').removeAttr('disabled');
            }
        }

        $('.add-repeatable').on('click', function(e) {
            e.preventDefault();
            addRepeatableBlock();
            checkMaxRepeatableCount();
        });
        $('body').on('click', '.remove-repeatable', function(e) {
            e.preventDefault();
            $(this).parent().remove();
            checkMaxRepeatableCount();
        });
        $('.save-button').on('click', () => {
            var data = {};
            $('[name]').each(function(elem, index) {
                data[$(elem).attr['name']] = $(elem).val();
            });
            console.info('common data is: ', data);


        })

        addCommonBlock();
        repeatableSchema && addRepeatableBlock();

    </script>
</body>
</html>