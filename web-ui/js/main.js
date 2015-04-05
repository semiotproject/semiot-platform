(function() {
	'use strict';

	// usage: "string {0} is {1}".format("one", "first");
	String.prototype.format = function() {
		var pattern = /\{\d+\}/g;
		var args = arguments;
		return this.replace(pattern, function(capture){ return args[capture.match(/\d+/)]; });
	};

	var app = angular.module('semiotApp', ['utils', 'dataProvider', 'utils' , "highcharts-ng"]);

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

	app.controller('MeterListCtrl', function($scope, dataProvider, utils) {
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

        $scope.convertType = utils.sparqlToHumanType;
	});

	app.controller('MeterSingleCtrl', function($scope, $interval, dataProvider, utils) {
		$scope.title = "";
		$scope.sensors = [];
		dataProvider.on("currentSystemChanged", function(system) {
			if (system !== null) {
				console.log(system);
				$scope.title = system.name;
				$scope.init(system.uri);
			}
		}); 

		$scope.init = function(uri) {
			// get extended info
			dataProvider.fetchSystemEndpoint(uri, function(data) {
				var sensors = [];
				data.results.bindings.forEach(function(binding) {
					var sensor = {
						testimonials: [],
						endpoint: binding.endpoint.value,
						type: binding.type.value,
						chartConfig: utils.getChartConfig(utils.sparqlToHumanType(binding.type.value), [])
					};
					// get TSDB archive testimonial

					// subscribe on topic							
					utils.subscribe(
						CONFIG.URLS.messageBus,
						utils.parseTopicFromEndpoint(sensor.endpoint),
						function(args) {
							console.info("Updating sensor {0}, message: {1}", sensor.type, args[0]);
						}
					);	

					$interval(function() {
						sensor.chartConfig.series[0].data.push(Math.random());
					}, 2000);	

					sensors.push(sensor);
				});
				$scope.sensors = sensors;
			});
		}
	});
}()); 
