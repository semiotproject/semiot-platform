<%@page import="java.util.Set"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="javax.json.JsonArray"%>
<%@page import="javax.json.JsonObject"%>
<%@page import="javax.json.JsonReader"%>
<%@page import="java.util.Iterator"%>

<%
    JsonObject jsonConfig = (JsonObject) session.getAttribute("jsonConfig");
    String pid = jsonConfig.getString("semiot:driverPid");
    JsonArray jsonArray = jsonConfig.getJsonArray("semiot:view");
    Integer repeatbleMax;
    if (jsonConfig.containsKey("semiot:maxRepeatableSchemas")) {
        repeatbleMax = jsonConfig.getInt("semiot:maxRepeatableSchemas");
    } else {
        repeatbleMax = 0;
    }

    session.setAttribute("pid", pid);
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>SemIoT Platform | Driver Configuration</title>
    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700" type="text/css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.6/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-material-design/0.5.9/css/bootstrap-material-design.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.0/leaflet.css" />
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet.draw/0.2.3/leaflet.draw.css">
    <link rel="stylesheet" href="/css/common.css">
    <link rel="stylesheet" href="/css/drivers.css">
    <script>
        var DRIVER_SCHEMA = JSON.parse('<%=jsonArray%>');
        var MAX_REPEATABLE_COUNT = parseInt('<%=repeatbleMax%>');
        var REPEATABLE_CONFIGURATIONS = [];
    </script>
    <style>
        i.fa.fa-info-circle {
            cursor: help;
            display: inline-block;
            margin-left: 5px;
            font-size: 18px;
            vertical-align: sub;
            display: none;
        }
    </style>
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
        <h3>
            <span>New driver configuration</span>
            <button class="btn btn-raised btn-primary btn-lg save-button">Save <i class="fa fa-save"></i></button>
        </h3>
        <form
            method="POST"
            action="${pageContext.request.contextPath}/config/ConfigurationDriver"
        >

            <div class="container well bs-component common-fields"></div>

            <h3 class="repeatable-configuration-header">
                <span>Repeatable configuration</span>
                <button class="btn btn-raised btn-primary btn-lg add-repeatable">Add <i class="fa fa-plus"></i></button>
            </h3>
            <div class="repeatable-configurations">

            </div>

        </form>
    </div>

    <div class="field template" style="display: none;">
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

    <form action="${pageContext.request.contextPath}/config/ConfigurationDriver" method="post" id='target-form' style="visibility: hidden;">
        <input type="hidden" name="save" value="Save and start">
    </form>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script src="https://fezvrasta.github.io/bootstrap-material-design/dist/js/material.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.0/leaflet.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet.draw/0.2.3/leaflet.draw.js"></script>
    <script>$.material.init();</script>
    <script>

        var AREA_TYPE = "semiot:GeoRectangle";

        var SCHEMAS = {
            common: (function() {
                return DRIVER_SCHEMA.filter((i) => {
                    return i['@type'] === 'semiot:CommonSchema';
                })[0];
            })(),
            repeatable: (function() {
                var result = DRIVER_SCHEMA.filter((i) => {
                    return i['@type'] === "semiot:RepeatableSchema";
                });
                if (result.length > 0) {
                    console.info('found repeatable schema: ', result[0]);
                    return result[0];
                }
            })()
        };

        var RESULT_CONFIG = {
            repeatable: {}
        };

        var UTILS = {
            getRectangeFromSchema: function(data) {
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
            },
            serializeRectangleToForm: function(prefix, rect) {
                var data = {};

                data[prefix + '1.latitude'] = rect[0][0];
                data[prefix + '1.longitude'] = rect[0][1];
                data[prefix + '2.latitude'] = rect[1][0];
                data[prefix + '2.longitude'] = rect[1][1];

                return data;
            },
            checkMaxRepeatableCount: function() {
                if ($('.repeatable-configurations > div').length === MAX_REPEATABLE_COUNT) {
                    $('.add-repeatable').attr('disabled', 'disabled');
                } else {
                    $('.add-repeatable').removeAttr('disabled');
                }
            },
            generateMapId: function() {
                return 'map' + Date.now();
            }
        };

        var LeafletMap = function(mapId, rectangle) {
            console.info('initializing new leaflet map');
            this._map = L.map(mapId);// .setView([51.505, -0.09], 13);

            this.addTileLayer();
            this.addDrawLayer();
            if (rectangle) {
                this.addDrawnItems([rectangle]);
                this.setCenter(rectangle[0]);
            }
            this.checkMaxRectangles();
        };
        LeafletMap.prototype = {
            constructor: LeafletMap,
            addTileLayer: function() {
                L.tileLayer('https://tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png', {
                    attribution: ''
                }).addTo(this._map);
            },
            addDrawLayer: function() {
                var that = this;
                this._drawnItems = new L.FeatureGroup();
                this._map.addLayer(this._drawnItems);

                this._drawControls = {
                    zero: new L.Control.Draw({
                        position: 'topright',
                        draw: {
                            rectangle: {},
                            polyline: false,
                            marker: false,
                            polygon: false,
                            circle: false
                        },
                        edit: {
                            featureGroup: this._drawnItems
                        }
                    }),
                    one: new L.Control.Draw({
                        position: 'topright',
                        draw: false,
                        edit: {
                            featureGroup: this._drawnItems
                        }
                    })
                }

                this._map.addControl(this._drawControls.zero);

                this._map.on('draw:created', function (e) {
                    that.clearItems();
                    that._drawnItems.addLayer(e.layer);
                    // that.checkMaxRectangles();
                });
                this._map.on('draw:edited', function (e) {
                    //
                });
                this._map.on('draw:deleted', function (e) {
                    // that.checkMaxRectangles();
                });
            },
            clearItems: function() {
                var that = this;
                this._drawnItems.getLayers().map(function(layer) {
                    that._drawnItems.removeLayer(layer);
                });
            },
            addDrawnItems: function(rectangles) {
                var that = this;
                rectangles.map(function(r) {
                    L.rectangle(r).addTo(that._drawnItems);
                });
            },
            setCenter: function(point) {
                this._map.setView(point, 13);
            },
            getRectangle: function() {
                try {
                    return this._drawnItems.getLayers()[0].getLatLngs().map(function(p) {
                        return [p.lat, p.lng];
                    }).filter(function(p, i) {
                        // to get only first and third points
                        return i % 2 === 0;
                    });
                } catch(e) {
                    console.error('failed to get rectangle from map: e = ', e);
                }
            },
            checkMaxRectangles: function() {
                /*
                if (Object.keys(this._drawnItems._layers).length >= 1) {
                    console.info('one rectangle on the map; hiding draw controls..');
                    this._drawControls.zero.removeFrom(this._map);
                    this._drawControls.one.addTo(this._map);
                } else {
                    console.info('zero rectangles on the map; showing draw controls..');
                    this._drawControls.one.removeFrom(this._map);
                    this._drawControls.zero.addTo(this._map);
                }
                */
            },
        };

        var DOM_BUILDERS = {
            field: function(f, key) {
                if (!f['rdfs:label']) {
                    return null;
                }
                var field = $('.field.template').find('.form-group').clone();

                field.find('label').text(f['rdfs:label']);
                field.find('input').val(f['dtype:defaultValue']).attr({
                    name: key
                });

                if (f['rdfs:comment']) {
                    field.find('label').append($('<i class="fa fa-info-circle" title="'+ f['rdfs:comment'] + '">'))
                }

                return field;
            },
            map: function(rectangle, container) {
                var map = $('.map-wrapper.template').find('> div').clone();
                var mapId = UTILS.generateMapId()
                map.attr({
                    id: mapId
                });
                map.appendTo(container);
                return new LeafletMap(mapId, rectangle);
            }
        };

        var BLOCK_BUILDERS = {
            common: function() {
                Object.keys(SCHEMAS.common).forEach(function(key, index) {
                    $('.common-fields').append(DOM_BUILDERS.field(SCHEMAS.common[key], key));
                });
            },
            repeatable: function() {
                var formTemplate = $('.repeatable-configuration.template').find('> div').clone();
                formTemplate.appendTo('.repeatable-configurations');

                var newRepeatableConfig = {};

                Object.keys(SCHEMAS.repeatable).forEach(function(key, index) {
                    if (key !== "@type") {
                        if (SCHEMAS.repeatable[key]['@type'] === AREA_TYPE) {
                            newRepeatableConfig[key] = $.extend({}, SCHEMAS.repeatable[key], {
                                map: DOM_BUILDERS.map(UTILS.getRectangeFromSchema(SCHEMAS.repeatable[key]), formTemplate)
                            });
                        } else {
                            var field = DOM_BUILDERS.field(SCHEMAS.repeatable[key], key);
                            newRepeatableConfig[key] = $.extend({}, SCHEMAS.repeatable[key], {
                                field: field
                            });
                            formTemplate.append(field);
                        }
                    }
                });

                var newRepeatableConfigIndex = Object.keys(RESULT_CONFIG.repeatable).length;

                RESULT_CONFIG.repeatable[newRepeatableConfigIndex] = newRepeatableConfig;
                formTemplate.find('.remove-repeatable').attr({
                    'data-index': newRepeatableConfigIndex
                });
            }
        };

        var GETTERS = {
            common: function() {
                var data = {};
                $('.common-fields [name]').each(function(index, elem) {
                    console.log( $(elem).val());
                    data[$(elem).attr('name')] = $(elem).val();
                });
                console.info('common config is: ', data);
                return data;
            },
            repeatable: function() {
                var data = {};
                Object.keys(RESULT_CONFIG.repeatable).map(function(key) {
                    return r = RESULT_CONFIG.repeatable[key];
                }).filter(function(r) {
                    return r;
                }).map(function(r, i) {
                    Object.keys(r).map(function(key, j) {
                        if (r[key]) {
                            var prefix = (i + 1).toString() + "." + key + ".";
                            if (r[key]['@type'] === AREA_TYPE) {
                                // get value from LeafletMap
                                var rectangle = r[key].map.getRectangle();
                                data = $.extend(data, UTILS.serializeRectangleToForm(prefix, rectangle));
                            } else {
                                // get value from DOM
                                data[prefix] = r[key].field.val();
                            }
                        }
                    });
                });
                console.info('repeatable config is: ', data);
                return data;
            }
        };

        function addEventListeners() {
            $('.add-repeatable').on('click', function(e) {
                e.preventDefault();
                BLOCK_BUILDERS.repeatable();
                UTILS.checkMaxRepeatableCount();
            });
            $('body').on('click', '.remove-repeatable', function(e) {
                e.preventDefault();
                $(this).parent().remove();
                UTILS.checkMaxRepeatableCount();
                RESULT_CONFIG.repeatable[$(this).attr('data-index')] = null;
            });
            $('.save-button').on('click', () => {
                var data = $.extend({}, GETTERS.common(), GETTERS.repeatable());
                console.info('result config is: ', data);
                submit(data);
            })
        }

        function submit(data) {
            Object.keys(data).map(function(key) {
                $('#target-form').append('<input name="' + key + '" value="' + data[key] + '">');
            });
            // $('#target-form').attr({ action: '/' });
            $('#target-form').submit();
        }

        function init() {
            if (SCHEMAS.common) {
                BLOCK_BUILDERS.common();
            } else {
                $('.common-fields').hide();
            }
            if (SCHEMAS.repeatable) {
                BLOCK_BUILDERS.repeatable();
            } else {
                $('.repeatable-configuration-header').hide();
            }
            addEventListeners();
        }

        init();
    </script>
</body>
</html>
