"use strict";

export default ["$routeProvider", "$locationProvider", function($routeProvider, $locationProvider) {
    $routeProvider
        .when('/', {
            templateUrl: '/partials/system-list.html',
            controller: 'SystemListCtrl'
        })
        .when('/:system_id*', {
            templateUrl: '/partials/system-detail.html',
            controller: 'SystemDetailCtrl'
        });

    // use the HTML5 History API
    $locationProvider.html5Mode(true);
}];