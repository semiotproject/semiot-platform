"use strict";

export default function($routeProvider) {
    $routeProvider
        .when('/', {
            redirectTo: '/systems'
        })
        .when('/login', {
            templateUrl: '/static/partials/login.html',
            controller: 'LoginCtrl'
        })
        .when('/systems', {
            templateUrl: '/static/partials/system-list.html',
            controller: 'SystemListCtrl'
        })
        .when('/systems/:system_uri*', {
            templateUrl: '/static/partials/system-detail.html',
            controller: 'SystemDetailCtrl'
        })
        .when('/queries', {
            templateUrl: '/static/partials/query-list.html',
            controller: 'QueryListCtrl'
        })
        .when('/queries/:query_id', {
            templateUrl: '/static/partials/query-detail.html',
            controller: 'QueryDetailCtrl'
        })
        .when('/new-query', {
            templateUrl: '/static/partials/new-query.html',
            controller: 'NewQueryCtrl'
        });
}
