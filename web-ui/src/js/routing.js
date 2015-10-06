"use strict";

export default function($routeProvider) {
    $routeProvider
        .when('/', {
            redirectTo: '/list'
        })
        .when('/login', {
            templateUrl: 'partials/login.html',
            controller: 'LoginCtrl'
        })
        .when('/list', {
            templateUrl: 'partials/list.html',
            controller: 'MeterListCtrl'
        })
        .when('/analyze', {
            templateUrl: 'partials/analyze.html',
            controller: 'AnalyzeCtrl'
        })
        .when('/single/:system_uri*', {
            templateUrl: 'partials/single.html',
            controller: 'MeterSingleCtrl'
        });
}
