"use strict";

export default function($scope, systemList, commonUtils) {
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
        let total_systems = systemList.getSystems().filter(function(s) {
            return !$scope.search.name || s.name.indexOf($scope.search.name) > -1;
        });
        $scope.systems = total_systems.slice(
            ($scope.pagination.currentPage - 1) * $scope.pagination.itemsPerPage,
            ($scope.pagination.currentPage) * $scope.pagination.itemsPerPage
        );
        $scope.pagination.totalItems = total_systems.length;
    };
    $scope.removeSystem = function(uri) {
        systemList.removeSystem(uri);
    };
    systemList.on('systemsUpdate', function(data) {
        $scope.setPagination();
    });
    systemList.fetchSystems().then(function(data) {
        $scope.setPagination();
    });
}