(function() {
	'use strict';

	var app = angular.module('semiotApp', ['utils', 'dataProvider', 'utils']);

	app.controller('AppCtrl', function($scope) {
		$scope.mode = "List"; // or "Single";
		$scope.showSingle = function(index) {
			$scope.mode = "Single";
			// dataProvider.currentSystem = index;
			console.log('showing singlem, index = ', index);
		};
		$scope.showList = function() {
			$scope.mode = "List";
			// dataProvider.currentSystem = null;
			console.log('showing list');
		};
	});

	app.controller('MeterListCtrl', function($scope, dataProvider, utils) {
		$scope.meters = [];
		$scope.search = {			
			types: CONFIG.SPARQL.types,
			type: "",
			name: ""
		};

		$scope.filterFunction = function(element) {
			return (!$scope.search.type || element.type == $scope.search.type) &&
					(!$scope.search.name || element.name.indexOf($scope.search.name) > -1);
		};

		dataProvider.on('metersUpdate', function(data) {
			$scope.meters = data;
		});	
        
        dataProvider.getMeters();

        $scope.convertType = utils.sparqlToHumanType;
	});

	app.controller('MeterSingleCtrl', function($scope, dataProvider, utils) {
		// $scope.data = dataProvider.getMeters()[dataProvider.currentSystem];
	});

}()); 
