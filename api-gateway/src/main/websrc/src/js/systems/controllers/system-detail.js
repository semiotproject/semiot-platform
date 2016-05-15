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
        });
    };
    $scope.subscribe = function(sensor) {
        observationAPI.subscribeForNewObservations(sensor.endpoint, sensor.topic, (msg) => {
            console.info(`received new observation for endpoint ${sensor.endpoint}, topic ${sensor.topic}: `, msg);
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
                        sensor.endpoint = res.endpoint;
                        sensor.topic = res.topic;
                        $scope.subscribe(sensor);
                    });

                    $scope.sensors.push(sensor);
                });
            });
        });
    };

    $scope.init(CONFIG.URLS.base + location.pathname);
}