"use strict";

export default function($scope, systemList, commonUtils, CONFIG) {
    $scope.isLoading = true;
    $scope.systems = [];
    $scope.search = {
        name: ""
    };
    $scope.pagination = {
        currentPage: 1,
        itemsPerPage: 10,
        totalItems: 1,
        numPages: 4,
        maxSize: 4
    };

    $scope.setPagination = function() {
        let total_systems = systemList.getSystems().filter(function(s) {
            return !$scope.search.name || s.name.toLowerCase().indexOf($scope.search.name.toLowerCase()) > -1;
        });
        console.log(total_systems);
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
        $scope.isLoading = false;
        $scope.setPagination();
        systemList.subscribe();
    });

}