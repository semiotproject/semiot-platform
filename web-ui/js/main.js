(function() {
	'use strict';

	// usage: "string {0} is {1}".format("one", "first");
	String.prototype.format = function() {
		var pattern = /\{\d+\}/g;
		var args = arguments;
		return this.replace(pattern, function(capture){ return args[capture.match(/\d+/)]; });
	};

	// usage: var founded = myArray.find((a) => { return a % 2 === 0; })
	if (!Array.prototype.find) {
		Array.prototype.find = function(predicate) {
			if (this == null) {
				throw new TypeError('Array.prototype.find called on null or undefined');
			}
			if (typeof predicate !== 'function') {
				throw new TypeError('predicate must be a function');
			}
			var list = Object(this);
			var length = list.length >>> 0;
			var thisArg = arguments[1];
			var value;

			for (var i = 0; i < length; i++) {
				value = list[i];
				if (predicate.call(thisArg, value, i, list)) {
					return value;
				}
			}
			return undefined;
		};
	}

	var ng_dependencies = [ 
		"rdfUtils", 
		"commonUtils",
		"dataProvider",
		"highcharts-ng", 
		"ui.bootstrap.datetimepicker"
	];

	var app = angular.module('semiotApp', ng_dependencies);

	app.controller('AppCtrl', function($scope, dataProvider) {
		$scope.currentView = "List";
		$scope.showSingle = function(system) {
			dataProvider.trigger("currentSystemChanged", system);
			$scope.currentView = "Single";
		};
		$scope.showList = function() {
			dataProvider.trigger("currentSystemChanged", null);
			$scope.currentView = "List";
		};
	});

	app.controller('MeterListCtrl', function($scope, dataProvider, commonUtils) {
		$scope.systems = [];
		$scope.search = {	
			name: ""
		};

		$scope.filterFunction = function(element) {
			return (!$scope.search.type || element.type == $scope.search.type) &&
					(!$scope.search.name || element.name.indexOf($scope.search.name) > -1);
		};

		dataProvider.on('systemsUpdate', function(data) {
			$scope.systems = data;
		});	
        
        dataProvider.fetchSystems();

	});

	app.controller('MeterSingleCtrl', function($scope, $interval, dataProvider, commonUtils, rdfUtils) {

		$scope.setDefault = function() {
			$scope.title = "";
			$scope.sensors = [];
			if ($scope.connectionPull && $scope.connectionPull.length > 0) {		
				$scope.connectionPull.forEach(function(connection) {
					connection.close();
				});
			}	
			$scope.connectionPull = [];
			$scope.default_range = (function() {
				var end_date = (new Date()).getTime();
				var start_date = (new Date(end_date - 7 * 24 * 3600 * 1000)).getTime();
				return [start_date, end_date];
			})();
		};
		$scope.init = function(uri) {
			// get extended info
			dataProvider.fetchSystemEndpoint(uri, function(data) {
				var sensors = [];
				data.results.bindings.forEach(function(binding) {
					var sensor = {
						testimonials: [],
						endpoint: binding.endpoint.value,
						type: binding.type.value,
						chartConfig: commonUtils.getChartConfig(binding.type.value, [])
					};

					// get TSDB archive testimonial
					dataProvider.fetchArchiveTestimonials(sensor.endpoint).then(function(result) {
						if (result.data[0]) {
							var dps = result.data[0].dps;
							var localTime = (new Date()).getTime();
							for (var timestamp in dps) {
								if (timestamp * 1000 < localTime) {
									sensor.chartConfig.series[0].data.push([timestamp * 1000, dps[timestamp]]);
								}
							}

						}
						// here we will parse result.data 
						// and add it to sensor.chartConfig.series[0].data											

						// here we will subscribe on each sensor, for example: 				
						var connection = commonUtils.subscribe(
							CONFIG.URLS.messageBus,
							commonUtils.parseTopicFromEndpoint(sensor.endpoint),
							function(args) {
								$scope.onUpdated(sensor, args[0]);
							}
						);	
						/*var connection = $interval(function() {
							var str = commonUtils.getMockHeatObservation();
							$scope.onUpdated(sensor, str);
						}, 2000);*/	
						$scope.connectionPull.push(connection);
						
					});
					sensor.range = $scope.default_range;
					sensors.push(sensor);
				});
				$scope.sensors = sensors;
			});
		}
		$scope.onUpdated = function(sensor, data) {
			rdfUtils.parseTTL(data).then(function(triples) {
				var resource = rdfUtils.parseTriples(triples);
				var observationResult = parseFloat(resource.get(CONFIG.SPARQL.types.observationResult));
				sensor.chartConfig.series[0].data.push([(new Date()).getTime(), observationResult]);
			});
		}

		$scope.setDefault();

		dataProvider.on("currentSystemChanged", function(system) {
			if (system !== null) {
				console.log(system);
				$scope.title = system.name;
				$scope.init(system.uri);
			} else {
				$scope.setDefault();
			}
		}); 
	});
}()); 
