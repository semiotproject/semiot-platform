'use strict';

import moment from 'moment';

window.moment = moment;

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

const app = window.angular.module('semiotApp', [
    "highcharts-ng",
    "ui.bootstrap.datetimepicker",
    require('angular-ui-bootstrap'),
    require('angular-route')
]);

// constants
app.constant("CONFIG", require('./config'));

// utils
app.factory('commonUtils', require('./utils/common'));
app.factory('chartUtils', require('./utils/chart'));

// services
app.factory('WAMP', require('./services/wamp'));
app.factory('HTTP', require('./services/http'));

// api
app.factory('systemAPI', require('./api/systems'));
app.factory('sensorAPI', require('./api/sensors'));
app.factory('observationAPI', require('./api/observations'));
app.factory('processAPI', require('./api/processes'));
app.factory('currentUser', require('./api/user'));

// controllers
app.controller('SystemListCtrl', require('./controllers/system-list'));
app.controller('SystemDetailCtrl', require('./controllers/system-detail'));

app.config(require('./routing'));