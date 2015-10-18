"use strict";

export default function(
    $scope,
    $routeParams,
    $q,
    systemDetail,
    machineToolStates,
    commonUtils,
    wampUtils,
    chartUtils,
    rdfUtils,
    CONFIG
) {

    // reset params, clear WAMP connections, etc
    $scope.setDefault = function() {
        $scope.title = "";
        $scope.sensors = [];
        $scope.liveEnabled = true;
        $scope.isLoading = true;
        if ($scope.connectionPull && $scope.connectionPull.length > 0) {
            $scope.connectionPull.forEach(function(connection) {
                connection.close();
            });
        }
        $scope.connectionPull = [];
        $scope.default_range = (function() {
            // time difference between server and client
            // FIXME
            let now = (new Date()).getTime();
            let end_date = new Date(now);
            let start_date = (new Date(now - 24 * 3600 * 1000));
            return [start_date, end_date];
        })();
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
            let sensors = [];

            // create sensor list
            data.results.bindings.forEach(function(binding) {
                let sensor = {
                    endpoint: commonUtils.parseTopicFromEndpoint(binding.endpoint.value),
                    type: binding.type.value,
                    range: $scope.default_range,
                    isLoading: true,
                    chartConfig: {}
                };

                // set initial chart config
                $scope.initChart(sensor).then(() => {

                    // set chart data
                    $scope.fillChart(sensor).then(() => {

                        $scope.subscribe(sensor);

                        sensor.isLoading = false;
                        // append sensor
                        console.info('sensor is ready: ', sensor);
                        sensors.push(sensor);

                        // TODO: reset flag after all sensors are loaded
                        $scope.isLoading = false;
                    });
                });
            });

            $scope.sensors = sensors;
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
                sensor.chartConfig = chartUtils.getStateChartConfig(sensor.type, machineToolStates.getStates());
                defer.resolve();
            });
        } else {
            sensor.chartConfig = chartUtils.getObservationChartConfig(sensor.type);
            defer.resolve();
        }

        return defer.promise;
    };

    // calls after init and when range was changed
    // @return Promise
    $scope.fillChart = function(sensor) {
        let defer = $q.defer();

        // get TSDB archive testimonial
        systemDetail.fetchArchiveTestimonials(sensor.endpoint, sensor.range).then(function(result) {
            if ($scope.isStateSensor(sensor)) {
                sensor.chartConfig.series[0].data = chartUtils.parseStateChartData(result.data);
            } else {
                sensor.chartConfig.series[0].data = chartUtils.parseObservationChartData(result.data);
            }

           defer.resolve();
        });

        return defer.promise;
    };

    // WAMP support
    $scope.subscribe = function(sensor) {
        console.info(`subscribing to ${sensor.endpoint}...`);
        $scope.connectionPull.push(wampUtils.subscribe(
            CONFIG.URLS.messageBus,
            [
                {
                    topic: sensor.endpoint,
                    callback: function(args) {
                        $scope.onUpdated(sensor, args[0]);
                    }
                }
            ]
        ));
    };

    // determine if it is state or observation sensor
    $scope.isStateSensor = function(sensor) {
        // FIXME
        return sensor.type === "http://purl.org/NET/ssnext/machinetools#MachineToolWorkingState";
    };

    // event handlers

    $scope.onNowClicked = function(sensor) {
        sensor.range[1] = (new Date()).getTime();
    };
    $scope.onUpdated = function(sensor, data) {
        console.info(`received message from ${sensor.endpoint}: `, data);
        /*
        rdfUtils.parseTTL(data).then(function(triples) {
            let resource = rdfUtils.parseTriples(triples);
            let observationResult = parseFloat(resource.get(CONFIG.SPARQL.types.observationResult));
            sensor.chartConfig.series[0].data.push([(new Date()).getTime() + CONFIG.TIMEZONE_OFFSET, observationResult]);
        });
        */
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

// "http://purl.org/NET/ssnext/machinetools#MachineToolWorkingState"