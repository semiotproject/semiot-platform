"use strict";

import N3 from "n3";

// fixme
function parseIdFromURI(uri) {
    return uri.substring(uri.lastIndexOf('/') + 1);
}

export default function(
    $scope,
    $routeParams,
    $q,
    $interval,
    commonUtils,
    chartUtils,
    systemAPI,
    sensorAPI,
    observationAPI,
    CONFIG
) {

    /*
    // TP BE REFACTORED
    function getLastHourRange() {
        // time difference between server and client
        // FIXME
        let now = (new Date()).getTime();
        let end_date = new Date(now);
        let start_date = (new Date(now - 1 * 3600 * 1000));

        console.log(`last hour: from ${new Date(start_date)} to ${new Date(end_date)}`);

        return [start_date.getTime(), end_date.getTime()];
    }

    $scope.getModes = () => {
        return {
            'archive': '0',
            'real-time': '1'
        };
    };

    $scope.version = CONFIG.VERSION;

    // reset params, clear WAMP connections, etc
    $scope.setDefault = function() {
        $scope.title = "";
        $scope.topic = null;
        $scope.sensors = [];
        $scope.liveEnabled = true;
    };

    // call when page is loaded
    $scope.init = function(uri) {
        $scope.setDefault();
        $scope.isLoading = true;

        $scope.getSystemInfo(uri).then(() => {
            $scope.getSystemSensors(uri);
        });
    };

    $scope.getSystemInfo = (uri) => {
        console.info(`loading detail info about ${uri} system...`);

        let defer = $q.defer();

        systemDetail.fetchSystemName(uri, function(data) {
            if (data.results.bindings[0]) {
                $scope.title = `${data.results.bindings[0].label.value} / ${data.results.bindings[0].id.value}`;

                systemDetail.fetchSystemTopic(uri, function(data) {
                    if (data.results.bindings[0]) {
                        $scope.topic = data.results.bindings[0].topic.value;
                        $scope.subscribe();
                    }
                    defer.resolve();
                });
            } else {
                $scope.title = "Unknown system";
            }
        });

        return defer.promise;
    };
    $scope.getSystemSensors = (uri) => {
        console.info(`loading ${uri} sensors...`);

        systemDetail.fetchSystemSensors(uri, function(data) {
            $scope.sensors = [];

            // create sensor list
            data.results.bindings.forEach((binding) => {
                let sensor = $.extend({}, {
                    uri: binding.instance.value,
                    title: `${binding.propLabel.value}, ${binding.valueUnitLabel.value}`,
                    observationType: binding.observationType.value,
                    sensorType: binding.type.value,
                    range: getLastHourRange(),
                    mode: $scope.getModes()['real-time'],
                    chartConfig: {}
                });

                // set initial chart config
                $scope.initChart(sensor).then(() => {

                    // set chart data
                    $scope.fillChart(sensor).then(() => {

                        // append sensor
                        console.info('sensor ready; appending it to sensor list...');

                        let s = $.extend({}, sensor);
                        $scope.sensors.push(s);
                    });
                });
            });
            $scope.isLoading = false;
        });
    };

    // call once when sensor info is received.
    // set perticular chart config
    // @return Promise
    $scope.initChart = function(sensor) {
        let defer = $q.defer();

        if ($scope.isStateSensor(sensor)) {
            // load state list
            // FIXME use not machineToolState service, but abstract StateEnumList service depend on sensor.type
            machineToolStates.fetchStates().then(() => {
                sensor.chartConfig = chartUtils.getStateChartConfig(sensor.title, machineToolStates.getStates());
                defer.resolve();
            });
        } else {
            sensor.chartConfig = chartUtils.getObservationChartConfig(sensor.title);
            defer.resolve();
        }

        return defer.promise;
    };

    // calls after init and when range was changed
    // @return Promise
    $scope.fillChart = function(sensor) {
        console.info(`setting new values to ${sensor.title} sensor..`);
        let defer = $q.defer();

        // get TSDB archive testimonial
        if ($scope.isStateSensor(sensor)) {
            systemDetail.fetchArchiveStates($scope.topic, sensor.range, sensor.type).then((result) => {
                sensor.chartConfig.series[0].data = chartUtils.parseStateChartData(result.data);
                defer.resolve();
            }, () => {
                console.error(`failed to load archive observations for some reason...`);
                sensor.chartConfig.series[0].data = [];
                defer.resolve();
            });
        } else {
            systemDetail.fetchArchiveObservations(
                parseIdFromURI(decodeURIComponent($routeParams.system_uri)),
                parseIdFromURI(sensor.uri),
                sensor.range
            ).then((result) => {
                sensor.chartConfig.series[0].data = chartUtils.parseObservationChartData(result.data);
                defer.resolve();
            }, () => {
                console.error(`failed to load archive observations for some reason...`);
                sensor.chartConfig.series[0].data = [];
                defer.resolve();
            });
        }

        return defer.promise;
    };

    // WAMP support
    $scope.subscribe = function() {
        wampUtils.subscribe([
            {
                topic: $scope.topic,
                callback: function(args) {
                    $scope.onObservationReceived(args[0]);
                }
            }
        ]);
    };
    $scope.unsubscribe = function() {
        wampUtils.unsubscribe($scope.topic);
    };
    $scope.onSetModeClick = function(sensor, mode) {
        if (sensor.mode !== mode) {
            sensor.mode = mode;
            if (mode === $scope.getModes()['real-time']) {
                $scope.subscribe(sensor);
                sensor.range = getLastHourRange();
                console.log(new Date(sensor.range[0]), new Date(sensor.range[1]));
                $scope.fillChart(sensor);
            } else {
                $scope.unsubscribe();
            }
        }
    };

    // determine if it is state or observation sensor
    $scope.isStateSensor = function(sensor) {
        return sensor.observationType === "http://www.qudt.org/qudt/owl/1.0.0/qudt/#Enumeration";
    };
    $scope.getSensorByURI = function(uri) {
        return $scope.sensors.find((s) => {
            return s.uri === uri;
        });
    };

    // event handlers

    $scope.onNowClicked = function(sensor) {
        sensor.range[1] = (new Date()).getTime();
    };
    $scope.onObservationReceived = function(data) {

        console.info(`received message: `, data);

        rdfUtils.parseTTL(data).then(function(triples) {

            let N3Store = N3.Store();

            N3Store.addPrefixes(CONFIG.SPARQL.prefixes);
            N3Store.addTriples(triples);

            let obs = N3Store.find(null, "rdf:type", "ssn:Observation", "")[0].subject;
            let sensorURI = N3Store.find(obs, "ssn:observedBy", null, "")[0].object;
            let obsResult = N3Store.find(obs, "ssn:observationResult", null, "")[0].object;
            let obsResultValue = N3Store.find(obsResult, "ssn:hasValue", null, "")[0].object;

            const sensor = $scope.getSensorByURI(sensorURI);

            if ($scope.isStateSensor(sensor)) {

                let state =  N3Store.find(obsResultValue, "ssn:hasValue", null, "")[0].object;

                sensor.chartConfig.series[0].data.push([(new Date()).getTime(), chartUtils.parseStateChartValue(state)]);
                console.info(`appended new state ${state}: now chartConfig data  is `, sensor.chartConfig);
            } else {

                let quantity = N3Store.find(obsResultValue, "qudt:quantityValue", null, "")[0].object;

                sensor.chartConfig.series[0].data.push([(new Date()).getTime() + CONFIG.TIMEZONE_OFFSET, parseFloat(N3.Util.getLiteralValue(quantity))]);
                console.info(`appended new quantity ${parseFloat(N3.Util.getLiteralValue(quantity))}: now chartConfig data  is `, sensor.chartConfig.series[0]);

            }});

            // remove first observation
            sensor.chartConfig.series[0].data.shift();

            // updating time window
            sensor.range = getLastHourRange();
    };
    $scope.onSetRangeClick = function(index) {
        let sensor = $scope.sensors[index];
        sensor.isLoading = true;
        $scope.fillChart(sensor).then(() => {
            sensor.isLoading = false;
        });
    };

    $scope.init(decodeURIComponent($routeParams.system_uri));*/

    function getLastHourRange() {
        // time difference between server and client
        // FIXME
        let now = (new Date()).getTime();
        let end_date = new Date(now);
        let start_date = (new Date(now - 1 * 3600 * 1000));

        return [start_date.getTime(), end_date.getTime()];
    }

    $scope.system = null;
    $scope.sensors = [];

    $scope.getModes = () => {
        return {
            'archive': '0',
            'real-time': '1'
        };
    };

    $scope.onSetModeClick = function(sensor, mode) {
        if (sensor.mode !== mode) {
            sensor.mode = mode;
            if (mode === $scope.getModes()['real-time']) {
                $scope.subscribe(sensor);
                sensor.range = getLastHourRange();
                console.log(new Date(sensor.range[0]), new Date(sensor.range[1]));
                $scope.fillChart(sensor);
            } else {
                $scope.unsubscribe();
            }
        }
    };
    $scope.onSetRangeClick = function(index) {
        let sensor = $scope.sensors[index];
        sensor.isLoading = true;
        $scope.fillChart(sensor).then(() => {
            sensor.isLoading = false;
        });
    };
    $scope.onNowClicked = function(sensor) {
        sensor.range[1] = (new Date()).getTime();
    };

    $scope.initChart = function(sensor) {
        sensor.observations = [];
        sensor.mode = $scope.getModes()['real-time'];
        sensor.chartConfig = chartUtils.getObservationChartConfig(sensor.label);
        sensor.range = getLastHourRange();
    };
    $scope.fillChart = function(sensor) {
        return observationAPI.loadObservations(sensor.observationsURI, sensor.range).then((obs) => {
            sensor.chartConfig.series[0].data = chartUtils.observationsToSerie(obs);
            console.log(chartUtils.observationsToSerie(obs));
        });
    };
    $scope.subscribe = function(endpoint, topic, sensor) {
        observationAPI.subscribeForNewObservations(endpoint, topic, (msg) => {
            console.info(`received new observation for endpoint ${endpoint}, topic ${topic}: `, msg);
            sensor.chartConfig.series[0].data.push(chartUtils.observationsToChartPoint(msg));

            // why view is not updating without $apply()?
            $scope.$apply();
        });
    };
    $scope.unsubscribe = function(sensor) {
        observationAPI.unsubscribeFromNewObservations(sensor);
    };

    $scope.init = function(uri) {
        console.info(`intializing system with uri = ${uri}`);
        systemAPI.loadSystem(uri).then((system) => {
            $scope.system = system;
            system.sensors.map((s, index) => {
                sensorAPI.loadSensor(s.uri).then((sensor) => {
                    console.info(`loaded sensor: `, sensor);

                    $scope.initChart(sensor);
                    $scope.fillChart(sensor);
                    observationAPI.loadWAMPTopic(sensor.observationsURI).then((res) => {
                        console.info(`loaded WAMP topic: `, res);
                        $scope.subscribe(res.endpoint, res.topic, sensor);
                    });

                    $scope.sensors.push(sensor);
                });
            });
        });
    };

    $scope.init(CONFIG.URLS.base + location.pathname);
}