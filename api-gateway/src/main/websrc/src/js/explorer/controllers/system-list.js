"use strict";

export default function($scope, systemAPI) {
    $scope.isLoading = true;
    $scope.totalSystems = [];
    $scope.filteredSystems = [];
    $scope.viewableSystems = [];
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
        $scope.filteredSystems = $scope.totalSystems.filter(function(s) {
            return !$scope.search.name || s.name.toLowerCase().indexOf($scope.search.name.toLowerCase()) > -1;
        });
        $scope.systems = $scope.filteredSystems.slice(
            ($scope.pagination.currentPage - 1) * $scope.pagination.itemsPerPage,
            ($scope.pagination.currentPage) * $scope.pagination.itemsPerPage
        );
        $scope.pagination.totalItems = $scope.filteredSystems.length;
    };

    $scope.handleNewSystem = (system) => {
        console.info('new system registered: ', system);
        system.index = $scope.totalSystems.length;
        $scope.totalSystems.push(system);
        $scope.setPagination();

        // why view is not updating without $apply()?
        $scope.$apply();
    };

    $scope.init = () => {
        systemAPI.loadSystems().then((res) => {
            $scope.totalSystems = res;
            systemAPI.subscribeForNewSystems($scope.handleNewSystem.bind($scope));
            $scope.setPagination();
            $scope.isLoading = false;
        });
    };

    $scope.init();

}