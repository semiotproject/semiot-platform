(function() {
	'use strict';

	var app = angular.module('semiotApp', ['utils', 'dataProvider', 'utils']);

	app.controller('AppCtrl', function($scope) {
		$scope.tabs = [
			{
				title: 'Heat',
				section: 'heat-section'
			},
			{
				title: 'Electric',
				section: 'electric-section'
			}
		];
		$scope.activeTab = 'Heat';

		$scope.toggleTabs = function(tab) {
			$scope.activeTab = tab;
		};
	});

	app.controller('MeterCtrl', function($scope, dataProvider, utils) {
		$scope.meters = [];
		$scope.search = {			
			types: CONFIG.SPARQL.types,
			type: "",
			uri: ""
		};

		$scope.filterFunction = function(element) {
			return (!$scope.search.type || element.type == $scope.search.type) &&
					(!$scope.search.uri || element.uri.indexOf($scope.search.uri) > -1);
		};

		dataProvider.on('metersUpdate', function(data) {
			$scope.meters = data;
		});	
        
        dataProvider.getMeters();

        $scope.convertType = utils.sparqlToHumanType;
	});
/*
	app.controller('ElectricCtrl', function($scope, dataProvider) {
		$scope.title = "Electric Meters";
		$scope.meters = [];

		dataProvider.on('electricMetersUpdate', function(data) {
			$scope.meters = data;
		});
        
               dataProvider.getElectricMeters();
	});
*/
}()); 
