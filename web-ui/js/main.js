(function() {
	'use strict';

	var app = angular.module('semiotApp', ['utils', 'dataProvider']);

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

	app.controller('HeatCtrl', function($scope, dataProvider) {
		$scope.title = "Heat Meters";
		$scope.meters = [];

		dataProvider.on('heatMetersUpdate', function(data) {
			$scope.meters = data;
		});
        
         dataProvider.getHeatMeters();
	});

	app.controller('ElectricCtrl', function($scope, dataProvider) {
		$scope.title = "Electric Meters";
		$scope.meters = [];

		dataProvider.on('electricMetersUpdate', function(data) {
			$scope.meters = data;
		});
        
               dataProvider.getElectricMeters();
	});

}()); 
