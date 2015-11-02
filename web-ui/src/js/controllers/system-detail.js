"use strict";

import N3 from "n3";

export default function(
    $scope,
    $routeParams,
    $q,
    $interval,
    systemDetail,
    machineToolStates,
    commonUtils,
    wampUtils,
    chartUtils,
    rdfUtils,
    CONFIG
) {

    function getLastHourRange() {
        // time difference between server and client
        // FIXME
        let now = (new Date()).getTime();
        let end_date = new Date(now);
        let start_date = (new Date(now - 1 * 3600 * 1000));
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
        $scope.sensors = [];
        $scope.liveEnabled = true;
        $scope.isLoading = true;
    };

    // call when page is loaded
    $scope.init = function(uri) {
        $scope.setDefault();

        // get extended info
        systemDetail.fetchSystemName(uri, function(data) {
            if (data.results.bindings[0]) {
                $scope.title = data.results.bindings[0].label.value;
            } else {
                $scope.title = "Unknown system";
            }
        });

        // get archive data
        systemDetail.fetchSystemEndpoint(uri, function(data) {
            $scope.sensors = [];

            // create sensor list
            data.results.bindings.forEach((binding) => {
                let sensor = $.extend({}, {
                    endpoint: commonUtils.parseTopicFromEndpoint(binding.endpoint.value),
                    title: `${binding.propLabel.value}, ${binding.valueUnitLabel.value}`,
                    observationType: binding.observationType.value,
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

                        $scope.subscribe(s);

                        /*
                        $interval(() => {
                            $scope.onObservationReceived(sensor, commonUtils.getMockMachineToolStateObservation());
                        }, 2000);
                        */
                    });
                });
            });
            $scope.isLoading = false;
        });
    };

    // call once when endpoint is received.
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
        console.info(`setting new values to ${sensor.endpoint} sensor..`);
        let defer = $q.defer();

        // get TSDB archive testimonial
        if ($scope.isStateSensor(sensor)) {
            systemDetail.fetchArchiveStates(sensor.endpoint, sensor.range).then(function(result) {
                sensor.chartConfig.series[0].data = chartUtils.parseStateChartData(result.data);
                defer.resolve();
            });
        } else {
            systemDetail.fetchArchiveObservations(sensor.endpoint, sensor.range).then(function(result) {
                sensor.chartConfig.series[0].data = chartUtils.parseObservationChartData(result.data);
                console.log(sensor);
                defer.resolve();
            });
        }

        return defer.promise;
    };

    // WAMP support
    $scope.subscribe = function(sensor) {
        wampUtils.subscribe([
            {
                topic: sensor.endpoint,
                callback: function(args) {
                    $scope.onObservationReceived(sensor, args[0]);
                }
            }
        ]);
    };
    $scope.unsubscribe = function(sensor) {
        wampUtils.unsubscribe(sensor.endpoint);
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
                $scope.unsubscribe(sensor);
            }
        }
    };

    // determine if it is state or observation sensor
    $scope.isStateSensor = function(sensor) {
        return sensor.observationType === "http://www.qudt.org/qudt/owl/1.0.0/qudt/#Enumeration";
    };

    // event handlers

    $scope.onNowClicked = function(sensor) {
        sensor.range[1] = (new Date()).getTime();
    };
    $scope.onObservationReceived = function(sensor, data) {
        // console.info(`received message from ${sensor.endpoint}: `, data);
        rdfUtils.parseTTL(data).then(function(triples) {

            let N3Store = N3.Store();

            N3Store.addPrefixes(CONFIG.SPARQL.prefixes);
            N3Store.addTriples(triples);

            let obs = N3Store.find(null, "rdf:type", "ssn:Observation", "")[0].subject;
            let obsResult = N3Store.find(obs, "ssn:observationResult", null, "")[0].object;
            let obsResultValue = N3Store.find(obsResult, "ssn:hasValue", null, "")[0].object;

            if ($scope.isStateSensor(sensor)) {

                let state =  N3Store.find(obsResultValue, "ssn:hasValue", null, "")[0].object;

                sensor.chartConfig.series[0].data.push([(new Date()).getTime(), chartUtils.parseStateChartValue(state)]);
                console.info(`appended new state: now chartConfig data  is `, sensor.chartConfig);
            } else {

                let quantity = N3Store.find(obsResultValue, "qudt:quantityValue", null, "")[0].object;

                sensor.chartConfig.series[0].data.push([(new Date()).getTime() + CONFIG.TIMEZONE_OFFSET, parseFloat(N3.Util.getLiteralValue(quantity))]);
                console.info(`appended new quantity ${parseFloat(N3.Util.getLiteralValue(quantity))}: now chartConfig data  is `, sensor.chartConfig.series[0]);

            }});

            // updating window
            $scope.range = $scope.getLastHourRange();
    };
    $scope.onSetRangeClick = function(index) {
        let sensor = $scope.sensors[index];
        sensor.isLoading = true;
        $scope.fillChart(sensor).then(() => {
            sensor.isLoading = false;
        });
    };

    $scope.init($routeParams.system_uri);
}