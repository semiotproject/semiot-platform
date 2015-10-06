"use strict";

export default function(
    $scope,
    $routeParams,
    $interval,
    dataProvider,
    commonUtils,
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
            let start_date = (new Date(now - 1 * 3600 * 1000));
            return [start_date, end_date];
        })();
    };
    $scope.init = function(uri) {
        // get extended info
        dataProvider.fetchSystemName(uri, function(data) {
            if (data.results.bindings[0]) {
                $scope.title = data.results.bindings[0].label.value;
            }
        });

        dataProvider.fetchSystemEndpoint(uri, function(data) {
            let sensors = [];
            data.results.bindings.forEach(function(binding) {
                let sensor = {
                    testimonials: [],
                    endpoint: binding.endpoint.value,
                    type: binding.type.value,
                    chartConfig: commonUtils.getChartConfig(binding.type.value, []),
                    range: $scope.default_range
                };

                // get TSDB archive testimonial

                dataProvider.fetchArchiveTestimonials(sensor.endpoint, sensor.range).then(function(result) {
                    sensor.chartConfig.series[0].data = commonUtils.normalizeTSDBData(result);

                    console.log(commonUtils.parseTopicFromEndpoint(sensor.endpoint));

                    let connection = commonUtils.subscribe(
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
                });
                sensors.push(sensor);
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
        dataProvider.fetchArchiveTestimonials(sensor.endpoint, sensor.range).then(function(result) {
            sensor.chartConfig.series[0].data = commonUtils.normalizeTSDBData(result);
            // sensor.chartConfig.xAxis.currentMin = (sensor.range[0]).getTime();
            // sensor.chartConfig.xAxis.currentMax = (sensor.range[1]).getTime();
        });
    };

    $scope.onNowClicked = function(sensor) {
        sensor.range[1] = (new Date()).getTime();
    };

    $scope.setDefault();

    $scope.init($routeParams.system_uri);
}