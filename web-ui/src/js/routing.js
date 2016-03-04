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
        .when('/systems/:system_uri*', {
            templateUrl: 'partials/system-detail.html',
            controller: 'SystemDetailCtrl'
        })
        .when('/queries', {
            templateUrl: 'partials/query-list.html',
            controller: 'QueryListCtrl'
        })
        .when('/queries/:query_id', {
            templateUrl: 'partials/query-detail.html',
            controller: 'QueryDetailCtrl'
        })
        .when('/new-query', {
            templateUrl: 'partials/new-query.html',
            controller: 'NewQueryCtrl'
        });
}
