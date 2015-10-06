"use strict";

export default function($scope, dataProvider, commonUtils) {
    $scope.systems = [];
    $scope.search = {
        name: ""
    };
    $scope.pagination = {
        currentPage: 1,
        itemsPerPage: 6,
        totalItems: 1,
        numPages: 4,
        maxSize: 4
    };

    $scope.setPagination = function() {
        let total_systems = dataProvider.getSystems().filter(function(s) {
            return !$scope.search.name || s.name.indexOf($scope.search.name) > -1;
        });
        $scope.systems = total_systems.slice(
            ($scope.pagination.currentPage - 1) * $scope.pagination.itemsPerPage,
            ($scope.pagination.currentPage) * $scope.pagination.itemsPerPage
        );
        $scope.pagination.totalItems = total_systems.length;
    };
    $scope.removeSystem = function(uri) {
        dataProvider.removeSystem(uri);
    };
    dataProvider.on('systemsUpdate', function(data) {
        $scope.setPagination();
    });
    dataProvider.fetchSystems().then(function(data) {
        $scope.setPagination();
    });
}