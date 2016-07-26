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
        .rectangle-editor > div > div,
        .circle-editor > div > div {
            display: inline-block;
            width: 120px;
            margin: 0 10px 0 0;
        }
        #map-editor {
            height: 300px;
        }
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
                        <li><a href="/logout">Logout</a></li>
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
        <div class="form-group is-empty form-wrapper">
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

    <div class="rectangle-field template" style="display: none;">
        <div class="form-group is-empty form-wrapper rectangle-editor">
            <label class="col-md-2 control-label"></label>
            <div class="col-md-10">
                <div class="form-group label-floating">
                    <input class="form-control location-lat-ne" type="text" placeholder="north-east latitude" />
                    <span class="help-block">North-East Latitude</code></span>
                </div>
                <div class="form-group label-floating">
                    <input class="form-control location-lng-ne" type="text" placeholder="north-east longitude" />
                    <span class="help-block">North-East Longitude</code></span>
                </div>
                <div class="form-group label-floating">
                    <input class="form-control location-lat-sw" type="text" placeholder="south-west latitude" />
                    <span class="help-block">South-West Latitude</code></span>
                </div>
                <div class="form-group label-floating">
                    <input class="form-control location-lng-sw" type="text" placeholder="south-west longitude" />
                    <span class="help-block">South-West Longitude</code></span>
                </div>
                <i class="fa fa-edit show-dialog"></i>
            </div>
            <span class="material-input"></span>
        </div>
    </div>

    <div class="circle-field template" style="display: none;">
        <div class="form-group form-wrapper is-empty circle-editor">
            <label class="col-md-2 control-label"></label>
            <div class="col-md-10">
                <div class="form-group label-floating">
                    <input class="form-control location-lat" type="text" placeholder="latitude" />
                    <span class="help-block">Latitude</code></span>
                </div>
                <div class="form-group label-floating">
                    <input class="form-control location-lng" type="text" placeholder="longitude" />
                    <span class="help-block">Longitude</code></span>
                </div>
                <div class="form-group label-floating">
                    <input class="form-control location-radius" type="text" placeholder="radius" />
                    <span class="help-block">Radius</code></span>
                </div>
                <i class="fa fa-edit show-dialog"></i>
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

    <div class="modal fade" id="edit-area-modal" tabindex="-1" role="dialog">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">Edit area</h4>
          </div>
          <div class="modal-body" id="modal-content">
            <div id="map-editor"></div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default modal-decline" data-dismiss="modal">Cancel</button>
            <button type="button" class="btn btn-primary modal-accept save-area-button">Save</button>
          </div>
        </div><!-- /.modal-content -->
      </div><!-- /.modal-dialog -->
    </div><!-- /.modal -->

    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
    <script src="//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script src="https://fezvrasta.github.io/bootstrap-material-design/dist/js/material.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.0/leaflet.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet.draw/0.2.3/leaflet.draw.js"></script>
    <script>$.material.init();</script>
    <script>

        var POLYGON_AREA_TYPE = "semiot:GeoRectangle";
        var CIRCLE_AREA_TYPE = "semiot:GeoCircle";

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
            serializeRectangleToForm: function(prefix, rect) {
                var data = {};

                data[prefix + '1.latitude'] = rect[0][0];
                data[prefix + '1.longitude'] = rect[0][1];
                data[prefix + '2.latitude'] = rect[1][0];
                data[prefix + '2.longitude'] = rect[1][1];
                debugger;

                return data;
            },
            serializeCircleToForm: function(prefix, circle) {
                var data = {};

                data[prefix + 'latitude'] = circle.latitude;
                data[prefix + 'longitude'] = circle.longitude;
                data[prefix + 'radius'] = circle.radius / 1000;

                return data;
            },
            checkMaxRepeatableCount: function() {
                if ($('.repeatable-configurations > div').length === MAX_REPEATABLE_COUNT) {
                    $('.add-repeatable').attr('disabled', 'disabled');
                } else {
                    $('.add-repeatable').removeAttr('disabled');
                }
            },
            getCurrentRepeatableCount: function() {
                return $('.repeatable-configurations > div').length;
            }
        };

        var LeafletMap = function(mapId) {
            console.info('initializing new leaflet map');
            this._map = L.map(mapId);
            this.setCenter([60, 30]);
            this.addTileLayer();
            this.addDrawLayer();
        };
        LeafletMap.prototype = {
            constructor: LeafletMap,
            invalidateSize: function() {
                this._map.invalidateSize();
            },
            addTileLayer: function() {
                L.tileLayer('https://tiles.wmflabs.org/bw-mapnik/{z}/{x}/{y}.png', {
                    attribution: ''
                }).addTo(this._map);
            },
            getDrawControls: function() {
                return {
                    zero: new L.Control.Draw({
                        position: 'topright',
                        draw: {
                            rectangle: {},
                            polyline: false,
                            marker: false,
                            polygon: false,
                            circle: {}
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
                };
            },
            addDrawLayer: function() {
                var that = this;
                this._drawnItems = new L.FeatureGroup();
                this._map.addLayer(this._drawnItems);
                this._drawControls = this.getDrawControls();
                this._map.addControl(this._drawControls.zero);
                this._map.on('draw:created', function (e) {
                    that.clearItems();
                    that._drawnItems.addLayer(e.layer);
                });
                this._map.on('draw:edited', function (e) {
                    //
                });
                this._map.on('draw:deleted', function (e) {

                });
            },
            clearItems: function() {
                var that = this;
                this._drawnItems.getLayers().map(function(layer) {
                    that._drawnItems.removeLayer(layer);
                });
            },
            setRectangles: function(rectangles) {
                this.clearItems();
                $('.leaflet-draw-draw-rectangle').show();
                $('.leaflet-draw-draw-circle').hide();
                this.addRectangles(rectangles);
            },
            addRectangles: function(rectangles) {
                var that = this;
                rectangles.map(function(r) {
                    debugger;
                    L.rectangle(r).addTo(that._drawnItems);
                });
            },
            setCircles: function(circles) {
                this.clearItems();
                $('.leaflet-draw-draw-rectangle').hide();
                $('.leaflet-draw-draw-circle').show();
                this.addCircles(circles);
            },
            addCircles: function(circles) {
                var that = this;
                circles.map(function(c) {
                    L.circle([c.latitude, c.longitude], c.radius).addTo(that._drawnItems);
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
            getCircle: function() {
                try {
                    var circle = this._drawnItems.getLayers()[0];
                    var latlng = circle.getLatLng();
                    var radius = parseFloat(circle.getRadius());
                    return {
                        latitude: latlng.lat,
                        longitude: latlng.lng,
                        radius: radius
                    };
                } catch(e) {
                    console.error('failed to get circle from map: e = ', e);
                }
            },
            remove: function() {
                this._map.remove();
            }
        };

        var DOM_BUILDERS = {
            field: function(f, key, type) {
                if (!f['rdfs:label']) {
                    return null;
                }

                var fieldSelector;
                switch (type) {
                case "text":
                    fieldSelector = '.field';
                    break;
                case "circle":
                    fieldSelector = '.circle-field';
                    break;
                case "rectangle":
                    fieldSelector = '.rectangle-field';
                    break;
                default:
                    return null;
                }

                var field = $(fieldSelector + '.template').find('.form-wrapper').clone();

                DOM_BUILDERS.addLabel(f, field);
                DOM_BUILDERS.addComment(f, field);

                switch (type) {
                case "text":
                    field.find('input').val(f['dtype:defaultValue']).attr({
                        name: key
                    });
                    break;
                case "circle":
                    SETTERS.circleToInputs({
                        latitude: f['ru.semiot.area.latitude']['dtype:defaultValue'],
                        longitude: f['ru.semiot.area.longitude']['dtype:defaultValue'],
                        radius: f['ru.semiot.area.radius']['dtype:defaultValue'] * 1000
                    }, field);

                    var onSave = function() {
                        SETTERS.circleToInputs(MAP.getCircle(), field);
                        // finally unbind event
                        $('body').off('click', '.save-area-button', onSave);
                        $('#edit-area-modal').modal('hide');
                    }

                    field.find('.show-dialog').on('click', function() {
                        console.info('showing circle editor dialog');
                        MAP.setCircles([GETTERS.circleFromInput(field)]);
                        $('body').on('click', '.save-area-button', onSave);
                    });
                    break;
                case "rectangle":
                    SETTERS.rectangleToInputs([
                        [
                            f['ru.semiot.area.latitude_ne']['dtype:defaultValue'],
                            f['ru.semiot.area.longitude_ne']['dtype:defaultValue']
                        ],
                        [
                            f['ru.semiot.area.latitude_sw']['dtype:defaultValue'],
                            f['ru.semiot.area.longitude_sw']['dtype:defaultValue']
                        ],
                    ], field);

                    var onSave = function() {
                        SETTERS.rectangleToInputs(MAP.getRectangle(), field);
                        // finally unbind event
                        $('body').off('click', '.save-area-button', onSave);
                        $('#edit-area-modal').modal('hide');
                    }

                    field.find('.show-dialog').on('click', function() {
                        console.info('showing rectangle editor dialog');
                        MAP.setRectangles([GETTERS.rectangleFromInput(field)]);
                        $('body').on('click', '.save-area-button', onSave);
                    });
                    break;
                default:
                    return null;
                }

                return field;
            },
            addLabel(f, field) {
                field.find('label').text(f['rdfs:label']);
            },
            addComment(f, field) {
                if (f['rdfs:comment']) {
                    field.find('label').append($('<i class="fa fa-info-circle" title="'+ f['rdfs:comment'] + '">'))
                }
            }
        };

        var BLOCK_BUILDERS = {
            common: function() {
                Object.keys(SCHEMAS.common).forEach(function(key, index) {
                    $('.common-fields').append(DOM_BUILDERS.field(SCHEMAS.common[key], key, 'text'));
                });
            },
            repeatable: function() {
                var formTemplate = $('.repeatable-configuration.template').find('> div').clone();
                formTemplate.appendTo('.repeatable-configurations');

                var newRepeatableConfig = {};

                Object.keys(SCHEMAS.repeatable).forEach(function(key, i) {
                    if (key !== "@type") {
                        var field;
                        if (SCHEMAS.repeatable[key]['@type'] === POLYGON_AREA_TYPE) {
                            field = DOM_BUILDERS.field(SCHEMAS.repeatable[key], key, 'rectangle');
                            newRepeatableConfig[key] = $.extend({}, SCHEMAS.repeatable[key], {
                                field: field
                            });
                            formTemplate.append(field);
                        } else if (SCHEMAS.repeatable[key]['@type'] === CIRCLE_AREA_TYPE) {
                            field = DOM_BUILDERS.field(SCHEMAS.repeatable[key], key, 'circle');
                            newRepeatableConfig[key] = $.extend({}, SCHEMAS.repeatable[key], {
                                field: field
                            });
                            formTemplate.append(field);
                        } else {
                            field = DOM_BUILDERS.field(SCHEMAS.repeatable[key], key, 'text');
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
                            var container = $('.repeatable-configurations').find('[data-index=' + j + ']').parent();
                            if (r[key]['@type'] === POLYGON_AREA_TYPE) {
                                var rectangle = GETTERS.rectangleFromInput(container.find('.rectangle-editor'));
                                data = $.extend(data, UTILS.serializeRectangleToForm(prefix, rectangle));
                            } else if (r[key]['@type'] === CIRCLE_AREA_TYPE) {
                                debugger;
                                var circle = GETTERS.circleFromInput(container.find('.circle-editor'));
                                data = $.extend(data, UTILS.serializeCircleToForm(prefix, circle));
                            } else {
                                // get value from DOM
                                data[prefix] = r[key].field.val();
                            }
                        }
                    });
                });
                console.info('repeatable config is: ', data);
                return data;
            },
            circleFromInput: function(el) {
                return {
                    latitude: el.find('.location-lat').val(),
                    longitude: el.find('.location-lng').val(),
                    radius: el.find('.location-radius').val() * 1000,
                }
            },
            rectangleFromInput: function(el) {
                return [
                    [
                        el.find('.location-lat-ne').val(),
                        el.find('.location-lng-ne').val()
                    ],
                    [
                        el.find('.location-lat-sw').val(),
                        el.find('.location-lng-sw').val()
                    ]
                ];
            }
        };

        var SETTERS = {
            circleToInputs: function(circle, field) {
                field.find('.location-lat').val(circle.latitude.toFixed(2)).attr({ name: '.ru.semiot.area.latitude' });
                field.find('.location-lng').val(circle.longitude.toFixed(2)).attr({ name: '.ru.semiot.area.longitude' });
                field.find('.location-radius').val((circle.radius / 1000).toFixed(2)).attr({ name: '.ru.semiot.area.radius' });
            },
            rectangleToInputs: function(rect, field) {
                field.find('.location-lat-ne').val(rect[0][0].toFixed(2)).attr({ name: '.ru.semiot.area.1.latitude' });
                field.find('.location-lng-ne').val(rect[0][1].toFixed(2)).attr({ name: '.ru.semiot.area.1.longitude' });
                field.find('.location-lat-sw').val(rect[1][0].toFixed(2)).attr({ name: '.ru.semiot.area.2.latitude' });
                field.find('.location-lng-sw').val(rect[1][1].toFixed(2)).attr({ name: '.ru.semiot.area.2.longitude' });
            }
        };

        var MAP = new LeafletMap('map-editor');

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
            $('.save-button').on('click', function() {
                var data = $.extend({}, GETTERS.common(), GETTERS.repeatable());
                console.info('result config is: ', data);
                submit(data);
            });
            $('body').on('click', '.show-dialog', function() {
                $("#edit-area-modal").modal({});
            });
            $("#edit-area-modal").on('show.bs.modal', function() {
                setTimeout(MAP.invalidateSize.bind(MAP), 500);
            });
        }

        function submit(data) {
            Object.keys(data).map(function(key) {
                $('#target-form').append('<input name="' + key + '" value="' + data[key] + '">');
            });
            $('#target-form').attr({ action: '/' });
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
