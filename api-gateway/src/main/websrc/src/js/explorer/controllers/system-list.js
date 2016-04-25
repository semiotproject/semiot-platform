"use strict";

export default function($scope, systemAPI) {
    $scope.isLoading = true;
    $scope.totalSystems = [];
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

    $scope.setPagination = function(totalSystems) {
        $scope.totalSystems = totalSystems.filter(function(s) {
            return !$scope.search.name || s.name.toLowerCase().indexOf($scope.search.name.toLowerCase()) > -1;
        });
        $scope.systems = $scope.totalSystems.slice(
            ($scope.pagination.currentPage - 1) * $scope.pagination.itemsPerPage,
            ($scope.pagination.currentPage) * $scope.pagination.itemsPerPage
        );
        $scope.pagination.totalItems = $scope.totalSystems.length;
    };

    $scope.handleNewSystem = (message) => {
        console.info('new system registered: ', message);
        //
    };

    $scope.init = () => {
        systemAPI.loadSystems().then((res) => {
            systemAPI.subscribeForNewSystems($scope.handleNewSystem.bind($scope));
            $scope.setPagination();
        });
    };

    $scope.init();

}