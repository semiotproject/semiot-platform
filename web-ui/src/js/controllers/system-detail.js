"use strict";

export default function(
    $scope,
    $routeParams,
    systemDetail,
    machineToolStates,
    commonUtils,
    wampUtils,
    rdfUtils,
    CONFIG
) {

    $scope.setDefault = function() {
        $scope.title = "";
        $scope.sensors = [];
        $scope.liveEnabled = true;
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

    $scope.init = function(uri) {

        // get extended info
        systemDetail.fetchSystemName(uri, function(data) {
            if (data.results.bindings[0]) {
                $scope.title = data.results.bindings[0].label.value;
            } else {
                $scope.title = "Unknown system";
            }
        });

        systemDetail.fetchSystemEndpoint(uri, function(data) {
            let sensors = [];
            data.results.bindings.forEach(function(binding) {
                let sensor = {
                    endpoint: binding.endpoint.value,
                    type: binding.type.value,
                    range: $scope.default_range,
                    chartConfig: {}
                };
                sensors.push(sensor);
                $scope.drawChart(sensor);
            });
            $scope.sensors = sensors;
        });
    };
    $scope.onUpdated = function(sensor, data) {
        rdfUtils.parseTTL(data).then(function(triples) {
            let resource = rdfUtils.parseTriples(triples);
            let observationResult = parseFloat(resource.get(CONFIG.SPARQL.types.observationResult));
            sensor.chartConfig.series[0].data.push([(new Date()).getTime() + CONFIG.TIMEZONE_OFFSET, observationResult]);
        });
    };
    $scope.setRange = function(index) {
        let sensor = $scope.sensors[index];
        $scope.drawChart(sensor);
    };

    $scope.drawChart = function (sensor) {

        // get TSDB archive testimonial
        systemDetail.fetchArchiveTestimonials(sensor.endpoint, sensor.range).then(function(result) {
            if (sensor.type ===  "http://purl.org/NET/ssnext/machinetools#MachineToolWorkingState") {
                sensor.chartConfig = commonUtils.getStepChartConfig(sensor.type, result.data);
            } else {
                sensor.chartConfig = commonUtils.getChartConfig(sensor.type, result.data);
            }

            /*
            console.log(commonUtils.parseTopicFromEndpoint(sensor.endpoint));
            let connection = wampUtils.subscribe(
                CONFIG.URLS.messageBus,
                [
                    {
                        topic: commonUtils.parseTopicFromEndpoint(sensor.endpoint),
                        callback: function(args) {
                            $scope.onUpdated(sensor, args[0]);
                        }
                    }
                ]
            );
            $scope.connectionPull.push(connection);
            */
        });
    };

    $scope.onNowClicked = function(sensor) {
        sensor.range[1] = (new Date()).getTime();
    };

    $scope.setDefault();

    machineToolStates.fetchStates().then(() => {
        $scope.init($routeParams.system_uri);
    });
}

// "http://purl.org/NET/ssnext/machinetools#MachineToolWorkingState"