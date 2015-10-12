"use strict";

export default function($routeProvider) {
    $routeProvider
        .when('/', {
            redirectTo: '/systems'
        })
        .when('/login', {
            templateUrl: 'partials/login.html',
            controller: 'LoginCtrl'
        })
        .when('/systems', {
            templateUrl: 'partials/system-list.html',
            controller: 'SystemListCtrl'
        })
        .when('/analyze', {
            templateUrl: 'partials/analyze.html',
            controller: 'AnalyzeCtrl'
        })
        .when('/systems/:system_uri*', {
            templateUrl: 'partials/system-detail.html',
            controller: 'SystemDetailCtrl'
        });
}
