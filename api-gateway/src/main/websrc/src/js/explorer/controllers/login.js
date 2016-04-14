"use strict";

export default function($scope, $location, loginService) {
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
    };
}