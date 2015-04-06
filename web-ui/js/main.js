(function() {
	'use strict';

	// usage: "string {0} is {1}".format("one", "first");
	String.prototype.format = function() {
		var pattern = /\{\d+\}/g;
		var args = arguments;
		return this.replace(pattern, function(capture){ return args[capture.match(/\d+/)]; });
	};

	var app = angular.module('semiotApp', ['commonUtils', 'rdfUtils', 'dataProvider', "highcharts-ng"]);

	app.controller('AppCtrl', function($scope, dataProvider) {
		$scope.currentView = "List";
		$scope.showSingle = function(index) {
			dataProvider.trigger("currentSystemChanged", dataProvider.getSystems()[index]);
			$scope.currentView = "Single"
			console.log('showing single, index = ', index);
		};
		$scope.showList = function() {
			dataProvider.trigger("currentSystemChanged", null);
			$scope.currentView = "List"
			console.log('showing list');
		};
	});

	app.controller('MeterListCtrl', function($scope, dataProvider, commonUtils) {
		$scope.systems = [];
		$scope.search = {			
			types: CONFIG.SPARQL.types,
			type: "",
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

        $scope.convertType = commonUtils.sparqlToHumanType;
	});

	app.controller('MeterSingleCtrl', function($scope, $interval, dataProvider, commonUtils, rdfUtils) {

		$scope.setDefault = function() {
			$scope.title = "";
			$scope.sensors = [];
			if (!$scope.connectionPull) {
				$scope.connectionPull = [];
			}			
			$scope.connectionPull.forEach(function(connection) {				
				// here will be something like connection.close();
				$interval.cancel(connection);
			});
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
						chartConfig: commonUtils.getChartConfig(commonUtils.sparqlToHumanType(binding.type.value), [])
					};

					// get TSDB archive testimonial
					dataProvider.fetchArchiveTestimonials(sensor.endpoint).then(function(result) {
						// here we will parse result.data 
						// and add it to sensor.chartConfig.series[0].data
						sensor.chartConfig.series[0].data.push(10, 4, 12, 44, 43, 40, 20);						

						// here we will subscribe on each sensor< for example: 				
						// commonUtils.subscribe(
						// 	CONFIG.URLS.messageBus,
						// 	commonUtils.parseTopicFromEndpoint(sensor.endpoint),
						// 	function(args) {
						// 		$scope.onUpdated(sensor, args[0]);
						// 	}
						// );	
						var connection = $interval(function() {
							var str = commonUtils.getMockHeatObservation();
							$scope.onUpdated(sensor, str);
						}, 2000);	
						$scope.connectionPull.push(connection);
						
					});
					sensors.push(sensor);
				});
				$scope.sensors = sensors;
			});
		}
		$scope.onUpdated = function(sensor, data) {
			rdfUtils.parseTTL(data).then(function(triples) {
				var resource = rdfUtils.parseTriples(triples);
				var observationResult = parseFloat(resource.get(CONFIG.SPARQL.types.observationResult));
				sensor.chartConfig.series[0].data.push(observationResult);
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