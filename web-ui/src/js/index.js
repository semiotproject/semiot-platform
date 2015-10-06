'use strict';

// usage: "string {0} is {1}".format("one", "first");
String.prototype.format = function() {
    let pattern = /\{\d+\}/g;
    let args = arguments;
    return this.replace(pattern, function(capture) { return args[capture.match(/\d+/)]; });
};

// usage: let founded = myArray.find((a) => { return a % 2 === 0; })
if (!Array.prototype.find) {
    Array.prototype.find = function(predicate) {
        if (this === null) {
            throw new TypeError('Array.prototype.find called on null or undefined');
        }
        if (typeof predicate !== 'function') {
            throw new TypeError('predicate must be a function');
        }
        let list = Object(this);
        let length = list.length >>> 0;
        let thisArg = arguments[1];
        let value;

        for (let i = 0; i < length; i++) {
            value = list[i];
            if (predicate.call(thisArg, value, i, list)) {
                return value;
            }
        }
        return undefined;
    };
}

import routing from './routing';
import LoginCtrl from './controllers/login';
import AnalyzeCtrl from './controllers/analyse';
import MeterListCtrl from './controllers/meter-list';
import MeterSingleCtrl from './controllers/meter-single';

let app = angular.module('semiotApp', [
    "highcharts-ng",
    require('angular-ui-bootstrap'),
    require('angular-route')
]);

// constants
app.constant("CONFIG", require('./config'));

// utils
app.factory('commonUtils', require('./utils/commonUtils'));
app.factory('rdfUtils', require('./utils/rdfUtils.js'));

// services
app.factory('loginService', require('./services/login-service'));

// models
app.factory('dataProvider', require('./models/data-provider'));

// controllers
app.controller('LoginCtrl', LoginCtrl);
app.controller('AnalyzeCtrl', AnalyzeCtrl);
app.controller('MeterListCtrl', MeterListCtrl);
app.controller('MeterSingleCtrl', MeterSingleCtrl);

app.config(routing);

app.run(['$rootScope', '$location', 'loginService', function ($rootScope, $location, loginService) {
    $rootScope.$on('$routeChangeStart', function (event) {
        if (!loginService.isLogged()) {
            $location.path('/login');
        }
    });
}]);