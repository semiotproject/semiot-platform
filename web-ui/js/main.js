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
		"loginService",
		"highcharts-ng", 
		"ui.bootstrap.datetimepicker",
		"ui.bootstrap",
		"ui.codemirror",
		"ngRoute"
	];

	var app = angular.module('semiotApp', ng_dependencies);

	app.config(function($routeProvider) {
		$routeProvider
			.when('/', {
				redirectTo: '/list'
			})
			.when('/login', {
				templateUrl: 'partials/login.html',
				controller: 'LoginCtrl'
			})
			.when('/list', {
				templateUrl: 'partials/list.html',
				controller: 'MeterListCtrl'
			})	
			.when('/analyze', {
				templateUrl: 'partials/analyze.html',
				controller: 'AnalyzeCtrl'
			})		
			.when('/single/:system_uri*', {
				templateUrl: 'partials/single.html',
				controller: 'MeterSingleCtrl'
			});
	});

	app.controller('LoginCtrl', function($scope, $location, loginService) {
		$scope.login = "";
		$scope.password = "";
		$scope.error = false;
		$scope.submit = function() {
			if (loginService.authenticate($scope.login, $scope.password)) {		
				$scope.login = "";
				$scope.password = "";		
				$location.path('/');	
			} else {
				$scope.error = true;
			}
		}
	});

	app.controller('AnalyzeCtrl', function($scope) {
		$scope.editorOptions = {
			lineWrapping : true,
			lineNumbers: true,
			matchBrackets: true,
			mode: 'application/sparql-query',
		};
		$scope.code = "fdsfds";
	});

	app.controller('MeterListCtrl', function($scope, dataProvider, commonUtils) {
		$scope.systems = [];
		$scope.search = {	
			name: ""
		};
		$scope.pagination = {
			currentPage: 1,
			itemsPerPage: 6,
			totalItems: 1,
			maxSize: 1000
		};

		$scope.setPagination = function() {
			var total_systems = dataProvider.getSystems().filter(function(s) {
				return !$scope.search.name || s.name.indexOf($scope.search.name) > -1;
			}) 
			$scope.systems = total_systems.slice(
				($scope.pagination.currentPage - 1) * $scope.pagination.itemsPerPage, 
				($scope.pagination.currentPage) * $scope.pagination.itemsPerPage
			);
			$scope.pagination.totalItems = total_systems.length;
		}
		$scope.removeSystem = function(uri) {
			dataProvider.removeSystem(uri);
		};
		dataProvider.on('systemsUpdate', function(data) {
			$scope.setPagination();
		});
		dataProvider.fetchSystems().then(function(data) {
			$scope.setPagination();
		});
	});

	app.controller('MeterSingleCtrl', function($scope, $routeParams, $interval, dataProvider, commonUtils, rdfUtils) {

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
				var now = (new Date()).getTime();
				var end_date = new Date(now);
				var start_date = (new Date(now - 1 * 3600 * 1000));
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
				var sensors = [];
				data.results.bindings.forEach(function(binding) {
					var sensor = {
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

						var connection = commonUtils.subscribe(
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
			debugger;
			rdfUtils.parseTTL(data).then(function(triples) {
				var resource = rdfUtils.parseTriples(triples);
				var observationResult = parseFloat(resource.get(CONFIG.SPARQL.types.observationResult));
				sensor.chartConfig.series[0].data.push([(new Date()).getTime() + CONFIG.TIMEZONE_OFFSET, observationResult]);
			});
		};	
		$scope.setRange = function(index) {
			var sensor = $scope.sensors[index];
			dataProvider.fetchArchiveTestimonials(sensor.endpoint, sensor.range).then(function(result) {
				sensor.chartConfig.series[0].data = commonUtils.normalizeTSDBData(result);
				//sensor.chartConfig.xAxis.currentMin = (sensor.range[0]).getTime();
				//sensor.chartConfig.xAxis.currentMax = (sensor.range[1]).getTime();
			});
		};

		$scope.onNowClicked = function(sensor) {
			sensor.range[1] = (new Date()).getTime();
		};

		$scope.setDefault();

		$scope.init($routeParams.system_uri);
	});

	app.run(['$rootScope', '$location', 'loginService', function ($rootScope, $location, loginService) {
	    $rootScope.$on('$routeChangeStart', function (event) {
			if (loginService.isLogged()) {

			} else {
				// $location.path('/login');
			}			
	    });
	}]);
}()); 
